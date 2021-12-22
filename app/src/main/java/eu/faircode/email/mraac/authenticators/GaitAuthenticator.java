package eu.faircode.email.mraac.authenticators;

import android.content.Context;
import android.content.Intent;

import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Adaptation;
import ca.uwaterloo.cs.crysp.libmraacintegration.auth.AuthCallback;
import ca.uwaterloo.cs.crysp.libmraacintegration.auth.BaseAuthenticator;

public class GaitAuthenticator extends BaseAuthenticator {
    public GaitAuthenticator(String id, Context context, AuthCallback callback) {
        super(id, context, callback);
    }

    @Override
    public void start(Adaptation adaptation) {
        super.start(adaptation);
        getContext().startService(new Intent(getContext(), GaitAuthService.class));
        adapt(adaptation);
    }

    @Override
    public void stop() {
        super.stop();
        getContext().stopService(new Intent(getContext(), GaitAuthService.class));
    }

    @Override
    public void adapt(Adaptation adaptation) {}
}
