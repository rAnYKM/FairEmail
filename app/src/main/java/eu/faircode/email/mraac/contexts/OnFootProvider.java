package eu.faircode.email.mraac.contexts;

import static eu.faircode.email.mraac.ExampleBYOD.CONTEXT_ONFOOT_ENTER;
import static eu.faircode.email.mraac.ExampleBYOD.CONTEXT_ONFOOT_EXIT;

import android.content.Context;
import android.content.Intent;

import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Adaptation;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Signal;
import ca.uwaterloo.cs.crysp.libmraacintegration.context.BaseContextProvider;

public class OnFootProvider extends BaseContextProvider {

    public OnFootProvider(String id, Context context) {
        super(id, context);
    }

    @Override
    public void start(Adaptation adaptation) {
        super.start(adaptation);
        Intent i = new Intent(getContext(), OnFootService.class);
        getContext().startService(i);
    }

    @Override
    public void stop() {
        super.stop();
        Intent i = new Intent(getContext(), OnFootService.class);
        getContext().stopService(i);
    }


    @Override
    public void adapt(Adaptation adaptation) {

    }

    @Override
    public void processResult(int result) {
        // in this function, you need to convert the raw result to signals
        switch (result) {
            case 1:
                sendContextSignal(new Signal(CONTEXT_ONFOOT_ENTER, getId()));
                break;
            case 0:
                sendContextSignal(new Signal(CONTEXT_ONFOOT_EXIT, getId()));
                break;
        }

    }
}