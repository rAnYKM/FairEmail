package eu.faircode.email.mraac;

import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.AuthSignals.EA_ACCEPT;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.AuthSignals.EA_REJECT;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.AuthSignals.IA_ACCEPT;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.AuthSignals.IA_REJECT;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.ServiceSignals.ADAPTATION_START;
import static ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.constants.ServiceSignals.ADAPTATION_STOP;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.MultiStageModel;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.MultiStageModelBuilder;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.AccessOp;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Adaptation;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.AdaptationPolicy;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.AdaptationScheme;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.AuthOp;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.RiskOp;

public class ExampleDummy {

    public static final int maxAuth = 2;
    public static final int maxSens = 2;
    public static final String CONTEXT_ONSITE = "ONSITE";
    public static final String CONTEXT_OFFSITE = "OFFSITE";
    public static final String CONTEXT_DUMMY = "adummycontext";
    public static final String CONTEXT_DUMMY_CONDITION = "dummycondition";
    public static final String AUTH_ITUS_ID = "itus";
    public static final String AUTH_GAIT_ID = "gait";
    public static final String AUTH_DUMMY_ID = "authdummy";
    public static final String CONTEXT_ONSITE_ID = "sentry";
    public static final String CONTEXT_DUMMY_ID = "contextdummy";
    public static final String CONTEXT_DUMMY2_ID = "contextdummy2";

    public static final String[] authSignals = {
            IA_ACCEPT, IA_REJECT, EA_ACCEPT, EA_REJECT
    };

    public static final int[][] transMatrix = {
            {-1, -1,  2,  0},
            { 1,  0, -1, -1},
            { 2,  1, -1, -1}
    };


    public static final String[] contextSignals = {
            CONTEXT_OFFSITE, CONTEXT_ONSITE
    };

    public static final String[] riskTypes = {
            "normal", "dummy"
    };

    public static final String[][] riskTrans = {
            {"normal", CONTEXT_DUMMY, "dummy"},
            {"dummy", CONTEXT_DUMMY, "normal"},
    };

    public static MultiStageModel buildModel() {
        Map<String, AuthOp> authOpMap = new HashMap<>();
        authOpMap.put(riskTypes[0], buildAuthOp(authSignals));
        authOpMap.put(riskTypes[1], buildAuthOp(authSignals));
        Map<String, AccessOp> accessOpMap = new HashMap<>();
        accessOpMap.put(riskTypes[0], buildAccessOp());
        accessOpMap.put(riskTypes[1], buildAccessOp());
        RiskOp riskOp = buildRiskOp(riskTrans);

        MultiStageModelBuilder builder = new MultiStageModelBuilder().setMaxAuthenticationLevel(maxAuth)
                .setMaxSensitivityLevel(maxSens).setRiskTypes(Arrays.asList(riskTypes))
                .setAuthOpMap(authOpMap).setAccessOpMap(accessOpMap).setRiskOp(riskOp);

        return builder.build();
    }

    public static RiskOp buildRiskOp(String[][] riskTrans) {
        Map<String, Map<String, String>> tmp = new HashMap<>();
        for (String[] riskTran : riskTrans) {
            Map<String, String> tmp2;
            if(tmp.containsKey(riskTran[0]) && tmp.get(riskTran[0]) != null) {
                tmp2 = tmp.get(riskTran[0]);
            } else {
                tmp2 = new HashMap<>();
            }
            assert tmp2 != null;
            tmp2.put(riskTran[1], riskTran[2]);
            tmp.put(riskTran[0], tmp2);
        }
        System.out.print(tmp.toString());
        return new RiskOp(tmp);
    }


    public static AuthOp buildAuthOp(String[] signals) {
        Map<Integer, Map<String, Integer>> tmp = new HashMap<>();
        for (int i = 0; i <= maxAuth; ++i) {
            Map<String, Integer> tmp2 = new HashMap<>();
            for (int j = 0; j < signals.length; ++j) {
                tmp2.put(signals[j], transMatrix[i][j]);
            }
            tmp.put(i, tmp2);
        }
        return new AuthOp(tmp);
    }

    public static AccessOp buildAccessOp() {
        Map<Integer, Map<Integer, Boolean>> tmp = new HashMap<>();
        for (int i = 0; i <= maxAuth; ++i) {
            Map<Integer, Boolean> tmp2 = new HashMap<>();
            for (int j = 1; j <= maxSens; ++j) {
                if (i >= j) {
                    tmp2.put(j, true);
                } else {
                    tmp2.put(j, false);
                }
            }
            tmp.put(i, tmp2);
        }
        return new AccessOp(tmp);
    }


    public static AdaptationScheme exampleAdaptationScheme() {
        AdaptationScheme scheme = new AdaptationScheme();
        Adaptation adaptation = new Adaptation(AUTH_DUMMY_ID, ADAPTATION_START);
        Adaptation adaptation1 = new Adaptation(AUTH_DUMMY_ID, ADAPTATION_STOP);
        scheme.setDefaultAuthenticators(Arrays.asList(adaptation));
        scheme.setAuthPolicies(Arrays.asList(new AdaptationPolicy(CONTEXT_DUMMY_CONDITION, Arrays.asList(adaptation1))));
        return scheme;
    }

    public static AdaptationScheme emptyAdaptationScheme() {
        AdaptationScheme scheme = new AdaptationScheme();
        return scheme;
    }
}
