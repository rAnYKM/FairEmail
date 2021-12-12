package eu.faircode.email.mraac.tool;

import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NextTaskGenerator extends TimerTask {
    private Timer timer;
    private List<Long> script;
    TaskCallback callback;
    private int ptr;
    boolean pause=false;

    public NextTaskGenerator(List<Long>script, int cur, Timer timer, TaskCallback callback) {
        this.script = script;
        this.ptr = cur;
        this.timer = timer;
        this.callback = callback;
    }

    @Override
    public void run() {
        callback.run();
        if (ptr + 1 < script.size() && !pause) {
            long time = (long) (script.get(ptr + 1)) - script.get(ptr);
            Log.i("Auth", "next event: " + time);
            NextTaskGenerator newGen = new NextTaskGenerator(script, ptr + 1, timer, callback);
            timer.schedule(newGen, time);
        }
    }

    public NextTaskGenerator pause() {
        pause = true;
        return this;
    }

    public void resume() {
        pause = false;
        if (ptr + 1 < script.size()) {
            long time = (long) (script.get(ptr + 1)) - script.get(ptr);
            Log.i("Auth", "next event: " + time);
            timer.schedule(new NextTaskGenerator(script, ptr + 1, timer, callback),
                    time);
        }
    }

    public int getPtr() {
        return ptr;
    }
}
