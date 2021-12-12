package eu.faircode.email.mraac;

import static eu.faircode.email.mraac.BYODAccessService.REPORT_RESOURCE_NAME;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import ca.uwaterloo.cs.crysp.libmraacintegration.ImplicitAuthActivity;
import ca.uwaterloo.cs.crysp.libmraacintegration.SecureActivity;
import ca.uwaterloo.cs.crysp.libmraacintegration.access.AccessControlService;

public class SecureBaseActivity extends ImplicitAuthActivity {

    private static final String TAG = "SecureBaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startService(new Intent(this, BYODAccessService.class));
        startService(new Intent(this, BYODContextService.class));
        startService(new Intent(this, BYODAuthenticationService.class));
        startService(new Intent(this, BYODAdaptationService.class));
        initReport();
        super.onCreate(savedInstanceState);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.get(
                                        Manifest.permission.ACCESS_FINE_LOCATION);
                                Boolean coarseLocationGranted = result.get(
                                        Manifest.permission.ACCESS_COARSE_LOCATION);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                } else {
                                    // No location access granted.
                                }
                            }
                    );

            // Before you perform the actual permission request, check whether your app
            // already has the permissions, and whether your app needs to show a permission
            // rationale dialog. For more details, see Request permissions.
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

    }

    @Override
    public void postFailedAuthentication() {
        // you need to implement something after the user fails to pass the authentication
        this.finish();
    }

    public void reportResourceName(String name) {
        Intent intent = new Intent(this, BYODAccessService.class);
        intent.setAction(REPORT_RESOURCE_NAME);
        intent.putExtra("name", name);
        Log.i(TAG, "report resource " + name);
        startService(intent);
    }

    public void initReport() {
        Log.i(TAG, this.getLocalClassName());
        reportResourceName(this.getLocalClassName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}