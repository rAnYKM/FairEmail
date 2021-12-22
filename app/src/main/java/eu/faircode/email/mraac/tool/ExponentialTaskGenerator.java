package eu.faircode.email.mraac.tool;

import android.util.Log;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import java.util.Timer;
import java.util.TimerTask;

public class ExponentialTaskGenerator extends TimerTask {
    private Timer timer;
    private ExponentialDistribution expd;
    TaskCallback callback;

    public ExponentialTaskGenerator(ExponentialDistribution expd, Timer timer, TaskCallback callback) {
        this.expd = expd;
        this.timer = timer;
        this.callback = callback;
    }

    @Override
    public void run() {
        callback.run();
        long time = (long) 2000; // (expd.sample() * 1000 + 1000);
        Log.i("Access", "next event: " + time);
        timer.schedule(new ExponentialTaskGenerator(expd, timer, callback),
                time);
    }

}