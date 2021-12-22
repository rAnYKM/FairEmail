package eu.faircode.email.mraac;

import ca.uwaterloo.cs.crysp.libmraacintegration.context.ContextService;
import eu.faircode.email.mraac.contexts.OnFootProvider;
import eu.faircode.email.mraac.contexts.OnsiteContextProvider;
import eu.faircode.email.mraac.contexts.ThresholdMovementProvider;

public class BYODContextService extends ContextService {
    @Override
    public void registerContextProviders() {
        addContextProviders(new OnsiteContextProvider(ExampleBYOD.CONTEXT_ONSITE_ID, this));
        addContextProviders(new OnFootProvider(ExampleBYOD.CONTEXT_ONFOOT_ID, this));
        // addContextProviders(new ThresholdMovementProvider(ExampleBYOD.CONTEXT_ONFOOT_ID, this));
//        addContextProviders(new DummyContextProvider(CONTEXT_DUMMY_ID, this));
//        addContextProviders(new DummyConditionProvider(CONTEXT_DUMMY2_ID, this));

    }
}
