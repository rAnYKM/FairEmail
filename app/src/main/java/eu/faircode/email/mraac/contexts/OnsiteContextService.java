package eu.faircode.email.mraac.contexts;

import static eu.faircode.email.mraac.ExampleBYOD.CONTEXT_ONSITE_ID;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import java.util.Timer;
import java.util.TimerTask;

import ca.uwaterloo.cs.crysp.libmraacintegration.context.ContextServiceBinder;
import eu.faircode.email.mraac.tool.ExponentialTaskGenerator;
import eu.faircode.email.mraac.tool.MockOutputProvider;
import eu.faircode.email.mraac.tool.TaskCallback;

public class OnsiteContextService extends Service implements ContextServiceBinder {

    /**
     * It consists of location sensors and network provider
     * Output: 0 not in the company, 1 in the company
     */

    private static final String TAG = "OnsiteContextService";
    private FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates = true;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private double[][] companyLocation= new double[][]{{43.472, -80.544}, {43.475,-80.54}};
    private int currentLocationState = 0;

    boolean mockMode = false;
    private static final double interval = 10;


    private ExponentialDistribution expd;
    private TimerTask timerTask;
    private Timer timer = new Timer();

    public OnsiteContextService() {
        if (mockMode) {
            expd = new ExponentialDistribution(interval);
            timerTask = new ExponentialTaskGenerator(expd, timer, new TaskCallback() {
                @Override
                public void run() {
                    currentLocationState = 1 - currentLocationState;
                    sendResult(OnsiteContextService.this, CONTEXT_ONSITE_ID, currentLocationState);
                }
            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "service created");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                    // Update UI with location data
                // Log.i("Location", "Now we are at " + location.getLatitude() + "," + location.getLongitude());

                if (!mockMode) {
                    if (location.getLatitude() < companyLocation[1][0] && location.getLatitude() > companyLocation[0][0]
                            && location.getLongitude() < companyLocation[1][1] && location.getLongitude() > companyLocation[0][1]) {
                        sendResult(OnsiteContextService.this, CONTEXT_ONSITE_ID, 0);
                    } else {
                        sendResult(OnsiteContextService.this, CONTEXT_ONSITE_ID, 0);
                    }
                }

            }
        };
        locationRequest = LocationRequest.create().
                setFastestInterval(15000).setInterval(15000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (requestingLocationUpdates) {
            Log.i(TAG, "start sensing the location");
            startLocationUpdates();
        }

        if (mockMode) {
            timer.schedule(timerTask, (long) expd.sample() * 1000);
        }

    }


    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}