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
import eu.faircode.email.mraac.authenticators.DummyAuthenticator;
import eu.faircode.email.mraac.authenticators.GaitAuthenticator;


/**
 * The collected authentication results are integers
 */
public class BYODAuthenticationService extends AuthenticationService {
    private static final String TAG = "BYODAUTH";
    private SimpleSlidingWindow ssw;

    public BYODAuthenticationService() {
        ssw = new SimpleSlidingWindow(1,1);
    }

    @Override
    public void registerAuthenticators() {
        /*
         * To create an authenticator instance, you need to specify its id (which is the identifier
         * in the adaptation policy), the service context, and a callback function regarding how
         * you process the result.
         *
         * By default, it should be
         * this, result -> aggregateResult(id, result)
         */
        // addAuthenticators(new ItusAuthenticator(AUTH_ITUS_ID,
        //        this, result -> aggregateResult(AUTH_ITUS_ID, result)));
        addAuthenticators(new GaitAuthenticator(AUTH_GAIT_ID,
                this, result -> aggregateResult(AUTH_GAIT_ID, result)));
       addAuthenticators(new DummyAuthenticator(CLIENT_AUTH_ID,
               // AUTH_DUMMY_ID,
//                this, result -> aggregateResult(AUTH_DUMMY_ID, result)));
        // addAuthenticators(new ClientAuthenticator(CLIENT_AUTH_ID,
                this, result -> {
            aggregateResult(CLIENT_AUTH_ID, result);
        }));
    }

    @Override
    public void aggregateResult(String authName, double result) {
        /*
         * Implement you score fusion scheme here.
         */
        if (result >= 0.5) {
            ssw.add(1);
        } else{
            ssw.add(0);
        }

        int significant = ssw.getQualifiedMajority();

        if (significant == 1) {
            // need to pack the method
            sendAuthResult(new Signal(IA_ACCEPT, authName));
        } else if (significant == 0) {
            sendAuthResult(new Signal(IA_REJECT, authName));
        }
    }

    @Override
    public void adaptAggregation(Adaptation adaptation) {
        // format: "-reset -m 1 -n 2
        // super.adaptAggregation(adaptation);
        SWParser parser = new SWParser();
        String[] items = adaptation.getArgument().split(" ");
        JCommander.newBuilder()
                .addObject(parser)
                .build()
                .parse(items);
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
