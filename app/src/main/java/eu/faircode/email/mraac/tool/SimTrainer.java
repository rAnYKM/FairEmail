package eu.faircode.email.mraac.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import eu.faircode.email.Log;

public class SimTrainer {

    private static final int CONV = 1000000;

    long baseTime = -1;
    List<double[]> data;
    List<Long> time;
    private int ptr;


    public SimTrainer(List<Long> time, List<double[]> data) {
        this.time = time;
        this.data = data;
        ptr = 0;
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime/CONV;
    }

    public double[] nextMatch(long t) {
        long delta = t/CONV - baseTime;
        while (ptr < time.size() && delta > time.get(ptr)) {
            ptr++;
        }
        if (ptr >= time.size()) return null;
        return data.get(ptr);
    }

    public boolean isReady() {
        return baseTime != -1;
    }

    public int getPtr() {
        return ptr;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public static SimTrainer fromInputStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<double[]> result = new ArrayList<>();
        List<Long> time = new ArrayList<>();
        if (is != null) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");
                double[] tmp = new double[items.length - 1];
                time.add((long) Double.parseDouble(items[0]));
                for (int i = 1; i < items.length; ++i) {
                    tmp[i - 1] = Double.parseDouble(items[i]);
                }
                result.add(tmp);
            }
        }
        return new SimTrainer(time, result);
    }
}
