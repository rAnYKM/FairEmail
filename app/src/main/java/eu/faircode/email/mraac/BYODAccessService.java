package eu.faircode.email.mraac;

import android.content.Intent;
import android.os.Handler;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.poi.ss.formula.functions.T;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ca.uwaterloo.cs.crysp.libmraacintegration.access.AccessControlService;
import eu.faircode.email.mraac.tool.ExponentialTaskGenerator;
import eu.faircode.email.mraac.tool.TaskCallback;

public class BYODAccessService extends AccessControlService {
    /*
    How to get the current resource name ?
    An observer of activity, once the current resource has change, observer.notify(currentResource)
     */


    // Signal signal = new Signal(ACCESS_SIGNAL_HEADER + level, TAG);
    //                LocalBroadcastManager.getInstance(this).sendBroadcast(signal.toIntent(SERVICE_ACCESS));

    public static final String REPORT_RESOURCE_NAME = "Report resource name";
    private static final boolean USE_FAKE = true;
    private static final double interval = 30;
    public static final String REQUEST_SENSITIVITY = "Request sensitivity";

    private ExponentialDistribution expd;
    private TimerTask timerTask;
    private Timer timer = new Timer();

    private int fakeSensitivity = 1;

    public BYODAccessService() {
        expd = new ExponentialDistribution(interval);
        timerTask = new ExponentialTaskGenerator(expd, timer, new TaskCallback() {
            @Override
            public void run() {
                fakeSensitivity = 3 - fakeSensitivity;
                sensitivityChange(fakeSensitivity);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, Integer> map = new HashMap<>();
        //map.put("eu.faircode.email.ActivityMain", 2);
        map.put("eu.faircode.email.ActivityCompose", 2);
        // map.put("eu.faircode.email.ActivityView", 2);

        setSensitivityDictionary(map);

        if (USE_FAKE) {
            timer.schedule(timerTask, (long) (expd.sample() * 1000));

        }
        setCurrentResource(initResourceRequest(), true);
    }

    @Override
    public String initResourceRequest() {
        return "eu.faircode.email.ActivityMain";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(REPORT_RESOURCE_NAME)) {
                String name = intent.getStringExtra("name");
                setCurrentResource(name);
            } else if (intent.getAction().equals(REQUEST_SENSITIVITY)) {
                sensitivityChange(getSensitivityLevel(currentResource));
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }




}
