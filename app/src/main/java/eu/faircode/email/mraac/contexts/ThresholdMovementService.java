package eu.faircode.email.mraac.contexts;

import static eu.faircode.email.mraac.ExampleBYOD.CONTEXT_ONFOOT_ID;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import ca.uwaterloo.cs.crysp.libmraacintegration.context.ContextServiceBinder;
import eu.faircode.email.mraac.authenticators.GaitData;
import eu.faircode.email.mraac.tool.FakeSensorDataProvider;
import eu.faircode.email.mraac.tool.SimTrainer;

public class ThresholdMovementService extends Service implements SensorEventListener, ContextServiceBinder {
    private static final String TAG = "MovementService";
    private static final boolean USE_FAKE_DATA = false;

    private static final int SAMPLING_RATE = 50;
    private static final int S_TO_US = 1000000;
    private static final int MIN_ACTIVATION_TIME = 5; // seconds

    private static final double HI_THRES = 1.8;
    private static final double LO_THRES = 0.3;
    private long lastActivationTime = -1;
    private boolean isMoving = false;
    private int counter = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    // for test and evaluation only
    // private FakeSensorDataProvider fakeAccProvider;
    private SimTrainer trainer;
    SharedPreferences.Editor editor1;
    SharedPreferences reader1;
    private boolean debug_flag = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (USE_FAKE_DATA) {
            reader1 = getSharedPreferences("mraac_debug", MODE_PRIVATE);
            try {
                InputStream is1 = getAssets().open(reader1.getString("basedir", ".") + "/tacc.txt");
                trainer = SimTrainer.fromInputStream(is1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (reader1.contains("basetime3") && reader1.getLong("basetime3", 0) > 0) {
                trainer.setBaseTime(reader1.getLong("basetime3", 0));
            }

        }
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        int interval = S_TO_US / SAMPLING_RATE;
        mSensorManager.registerListener(this, mAccelerometer, interval);

        Log.i(TAG, "I start to work!");

    }

    @Override
    public void onDestroy() {
        editor1 = reader1.edit();
        editor1.putLong("basetime3", trainer.getBaseTime()* S_TO_US);
        super.onDestroy();
        mSensorManager.unregisterListener(this); // , mAccelerometer);
        Log.i(TAG, "So long!");

        // mSensorManager.unregisterListener(this, mGyroscope);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double mag;
        if (USE_FAKE_DATA) {
            if (!trainer.isReady()) {
                trainer.setBaseTime(sensorEvent.timestamp);
            }
            double[] tmp = trainer.nextMatch(sensorEvent.timestamp);
            if (tmp == null) {
                if (!debug_flag)
                    Log.i("MRAAC_EXP", "THRES ALL OVER");
                debug_flag = true;
                return;
            }
            // Log.i(TAG, trainer.getPtr() + ", " + Arrays.toString(tmp));
            mag = getMagnitude(tmp);
        } else {
            mag = getMagnitude(sensorEvent.values);
        }

        mag = Math.abs(mag - 9.81);


        counter += 1;
        if (counter == 50*15)
            sendResult(this, CONTEXT_ONFOOT_ID, 1);

        if (counter == 50*30) {
            sendResult(this, CONTEXT_ONFOOT_ID, 0);
            counter = 0;
        }

        // return;

//        if (mag >= HI_THRES) {
//            if (!isMoving) {
//                isMoving = true;
//                lastActivationTime = sensorEvent.timestamp;
//                sendResult(this, CONTEXT_ONFOOT_ID, 1);
//            } else {
//                lastActivationTime = sensorEvent.timestamp;
//            }
//        } else if (mag <= LO_THRES) {
//            if (lastActivationTime == -1) {
//                sendResult(this, CONTEXT_ONFOOT_ID, 1);
//                lastActivationTime = 0;
//            }
//
//            if (isMoving && (sensorEvent.timestamp - lastActivationTime) / S_TO_US / 1000 >= MIN_ACTIVATION_TIME) {
//                isMoving = false;
//                sendResult(this, CONTEXT_ONFOOT_ID, 1);
//            }
//
//        }
    }

    public double getMagnitude(float[] values) {
        return Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
    }

    public double getMagnitude(double[] values) {
        return Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
