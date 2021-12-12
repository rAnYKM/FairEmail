package eu.faircode.email.mraac.authenticators;

import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.ServiceSignals.DEBUG_FORCE_NEGATIVE;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.ServiceSignals.DEBUG_FORCE_POSITIVE;
import static ca.uwaterloo.cs.crysp.libmraacintegration.auth.AuthenticationService.CLIENT_AUTH_ID;
import static eu.faircode.email.mraac.ExampleBYOD.AUTH_DUMMY_ID;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.poi.ss.formula.functions.T;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import eu.faircode.email.mraac.BYODContextService;
import eu.faircode.email.mraac.ExampleDummy;
import eu.faircode.email.mraac.tool.NextTaskGenerator;
import eu.faircode.email.mraac.tool.SimTrainer;
import eu.faircode.email.mraac.tool.TaskCallback;


public class DummyAuthService extends Service {
    private static final String TAG = "DummyAuthService";
//    private Timer timer;
//    private TimerTask task = new TimerTask() {
//        @Override
//        public void run() {
////            RandomProbability rp = new RandomProbability(new int[] {0, 1},
////                    new float[]{0.05f, 0.95f});
//            Intent intent = new Intent(AUTH_DUMMY_ID);
//            intent.putExtra("result", 1.0);
//            long timeAnchor = SystemClock.elapsedRealtimeNanos();
//            Log.i("Time Anchor", "authissue:" + timeAnchor);
//            LocalBroadcastManager.getInstance(DummyAuthService.this).sendBroadcast(intent);
//        }
//    };

    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(AUTH_DUMMY_ID);
            intent.putExtra("result", 1.0);
            long timeAnchor = SystemClock.elapsedRealtimeNanos();
            Log.i("Time Anchor", "authissue:" + timeAnchor);
            LocalBroadcastManager.getInstance(DummyAuthService.this).sendBroadcast(intent);
            // handler.postDelayed(runnableCode, 5000);
        }
    };

    NextTaskGenerator generator;
    List<double[]> trainer;
    List<Long> trainTime;
    Timer timer;
    int counter;

    SharedPreferences.Editor editor;
    SharedPreferences reader;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        counter = 0;
        // timer = new Timer();
        // timer.scheduleAtFixedRate(task, 2000, 5000); // 1s a result
        // handler.postDelayed(runnableCode, 500);
        try {
            // Log.e(TAG, "LOAD AGAIN");
            reader = getSharedPreferences("mraac_debug", MODE_PRIVATE);
            String host = reader.getString("hostdir", null);
            String attack = reader.getString("basedir", null);
            // InputStream is1 = getAssets().open(host + "/ttouch" + host + "-" + attack + ".txt");
            InputStream is1 = getAssets().open(host + "/ttouch.txt"); // + host + "-" + attack + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is1));
            trainer= new ArrayList<>();
            trainTime = new ArrayList<>();
            if (is1 != null) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] items = line.split(",");
                    trainTime.add((long)Double.parseDouble(items[0]));
                    trainer.add(new double[] {
                            Double.parseDouble(items[1]),
                            Double.parseDouble(items[2])
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        reader = getSharedPreferences("mraac_debug", MODE_PRIVATE);
//        int cur = 0;
//        long tmp = 0;
//        if (reader.contains("basetime4") && reader.getLong("basetime4", 0) > 0){
//            tmp = reader.getLong("basetime4", 0);
//            while (cur < trainTime.size() && trainTime.get(cur) <= tmp) cur++;
//        }

//        if (cur  < trainTime.size()) {

            timer = new Timer();
            generator = new NextTaskGenerator(trainTime, 0, timer,
                    new TaskCallback() {
                        @Override
                        public void run() {
                            double[] res = trainer.get(counter);
                            counter++;
                            Intent intent1 = new Intent(CLIENT_AUTH_ID);
                            intent1.putExtra("result", res[1]);
                            long timeAnchor = SystemClock.elapsedRealtimeNanos();
                            Log.i("Time Anchor", "dummyauthissue:" + timeAnchor);
                            Log.d("MRAAC_EXP", "touch " + res[1]);
                            LocalBroadcastManager.getInstance(DummyAuthService.this).sendBroadcast(intent1);
                        }
                    });
            timer.schedule(generator, (long) (trainTime.get(0)));
    //    }
        Log.i(TAG, "I start to work!");
    }

    @Override
    public void onDestroy() {
        long timeAnchor = SystemClock.elapsedRealtimeNanos();
        Log.i("Time Anchor", "authadapt:" + timeAnchor);
        // timer.cancel();
        editor = reader.edit();
        editor.putLong("basetime4", trainTime.get(counter));

        super.onDestroy();
        // handler.removeCallbacks(runnableCode);
        // Intent intent = new Intent(this, BYODContextService.class);
        // intent.setAction(DEBUG_FORCE_NEGATIVE);
        // startService(intent);
        Log.i(TAG, "So long!");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(DEBUG_FORCE_POSITIVE)) {
                Intent intent1 = new Intent(AUTH_DUMMY_ID);
                intent1.putExtra("result", 1.0);
                long timeAnchor = SystemClock.elapsedRealtimeNanos();
                Log.i("Time Anchor", "authissue:" + timeAnchor);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else if (intent.getAction().equals(DEBUG_FORCE_NEGATIVE)) {
                Intent intent1 = new Intent(AUTH_DUMMY_ID);
                intent1.putExtra("result", 0.0);
                long timeAnchor = SystemClock.elapsedRealtimeNanos();
                Log.i("Time Anchor", "authissue:" + timeAnchor);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
