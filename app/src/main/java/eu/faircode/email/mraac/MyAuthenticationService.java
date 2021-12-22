package eu.faircode.email.mraac;

import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.AuthSignals.IA_ACCEPT;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.AuthSignals.IA_REJECT;
import static eu.faircode.email.mraac.ExampleBYOD.AUTH_GAIT_ID;

import com.beust.jcommander.JCommander;

import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Adaptation;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Signal;
import ca.uwaterloo.cs.crysp.libmraacintegration.auth.AuthenticationService;
import ca.uwaterloo.cs.crysp.libmraacintegration.auth.ClientAuthenticator;
import ca.uwaterloo.cs.crysp.libmraacintegration.auth.SWParser;
import ca.uwaterloo.cs.crysp.libmraacintegration.auth.SimpleSlidingWindow;
import eu.faircode.email.Log;
import eu.faircode.email.mraac.authenticators.GaitAuthenticator;

public class MyAuthenticationService extends AuthenticationService {
    private static final String TAG = "BYODAUTH";
    private SimpleSlidingWindow ssw;

    public MyAuthenticationService() {
        ssw = new SimpleSlidingWindow(1,1);
    }

    @Override
    public void registerAuthenticators() {
        addAuthenticators(new GaitAuthenticator(AUTH_GAIT_ID,
                this, result -> aggregateResult(AUTH_GAIT_ID, result)));
        addAuthenticators(new ClientAuthenticator(CLIENT_AUTH_ID,
                this, result -> { aggregateResult(CLIENT_AUTH_ID, result); }));
    }

    @Override
    public void aggregateResult(String authName, double result) {
        if (result >= 0.0) ssw.add(1);
        else ssw.add(0);
        int significant = ssw.getQualifiedMajority();
        if (significant == 1) sendAuthResult(new Signal(IA_ACCEPT, authName));
        else if (significant == 0) sendAuthResult(new Signal(IA_REJECT, authName));
    }

    @Override
    public void adaptAggregation(Adaptation adaptation) {
        SWParser parser = new SWParser();
        String[] items = adaptation.getArgument().split(" ");
        JCommander.newBuilder().addObject(parser).build().parse(items);
        if (parser.isReset()) {
            Log.i(TAG, "Apply aggregation adaptation: reset");
            ssw.reset();
        }
        if (parser.getM() != null && parser.getN() != null) {
            if (parser.getM() != ssw.getM() || parser.getN() != ssw.getN()) {
                Log.i(TAG, "Apply aggregation adaptation: " + parser.getN() + ", " + parser.getM());
                ssw.resize(parser.getM(), parser.getN());
            }
        }
    }
}
