package eu.faircode.email.mraac.contexts;

import static eu.faircode.email.mraac.ExampleBYOD.CONTEXT_OFFSITE;
import static eu.faircode.email.mraac.ExampleBYOD.CONTEXT_ONSITE;

import android.content.Context;
import android.content.Intent;

import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Adaptation;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Signal;
import ca.uwaterloo.cs.crysp.libmraacintegration.context.BaseContextProvider;

public class OnsiteContextProvider extends BaseContextProvider {

    public OnsiteContextProvider(String id, Context context) {
        super(id, context);
    }

    @Override
    public void start(Adaptation adaptation) {
        super.start(adaptation);
        getContext().startService(new Intent(getContext(), OnsiteContextService.class));
    }

    @Override
    public void stop() {
        super.stop();
        getContext().stopService(new Intent(getContext(), OnsiteContextService.class));
    }

    @Override
    public void adapt(Adaptation adaptation) { }

    @Override
    public void processResult(int result) {
        if (result == 1) sendContextSignal(new Signal(CONTEXT_ONSITE, getId()));
        else sendContextSignal(new Signal(CONTEXT_OFFSITE, getId()));
    }
}
