package eu.faircode.email.mraac.contexts;

import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.ServiceSignals.DEBUG_FORCE_NEGATIVE;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.ServiceSignals.DEBUG_FORCE_POSITIVE;
import static eu.faircode.email.mraac.ExampleDummy.CONTEXT_DUMMY_ID;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import ca.uwaterloo.cs.crysp.libmraacintegration.context.ContextServiceBinder;

public class DummyContextService extends Service implements ContextServiceBinder {
    private static final String TAG = "DummyContextService";
    private static final String ALARMCALLBACK = "DummyContextALARMCALLBACK";
    private Timer timer;
    private final TimerTask task = new TimerTask() {
        @Override
        public void run() {
//            Intent intent = new Intent(DummyContextProvider.CONTEXT_NAME);
//            RandomProbability rp = new RandomProbability(new int[] {0, 1},
//                    new float[]{0.7f, 0.3f});
//            intent.putExtra("result", rp.nextInt());
//            LocalBroadcastManager.getInstance(DummyContextService.this).sendBroadcast(intent);
            sendResult(DummyContextService.this, CONTEXT_DUMMY_ID, 1);
        }
    };
    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            sendResult(DummyContextService.this, CONTEXT_DUMMY_ID, 1);
            handler.postDelayed(runnableCode, 2000);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // timer = new Timer();
        // timer.scheduleAtFixedRate(task, 1000, 5000);
        handler.postDelayed(runnableCode, 100);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnableCode);
        // timer.cancel();
        // alarmManager.cancel(pendingIntent);
    }

    PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, DummyContextService.class);
        intent.setAction(ALARMCALLBACK);
        return PendingIntent.getService(this, 3221,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(DEBUG_FORCE_POSITIVE)) {
                sendResult(this, CONTEXT_DUMMY_ID, 1);
            } else if (intent.getAction().equals(DEBUG_FORCE_NEGATIVE)) {
                sendResult(this, CONTEXT_DUMMY_ID, 0);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}