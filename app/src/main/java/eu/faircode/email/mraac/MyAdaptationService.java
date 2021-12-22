package eu.faircode.email.mraac;

import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.AdaptationService;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.MultiStageModel;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Stage;

public class MyAdaptationService extends AdaptationService {
    @Override
    public void initModel() {
        // BYOD Logic
        MultiStageModel model = ExampleBYOD.buildModel();
        for (Stage stage: model.getStages()) {
            if (stage.isLockedStage()) stage.setScheme((ExampleBYOD.emptyAdaptationScheme()));
            else if (stage.getRiskType().equals("offsite")) {
                if (stage.getAuthenticationLevel() == 2 && stage.getSensitivityLevel() == 1)
                    stage.setScheme(ExampleBYOD.offsiteScheme2());
                else if (stage.getAuthenticationLevel() == 2) stage.setScheme(ExampleBYOD.offsiteScheme1());
                else stage.setScheme(ExampleBYOD.offsiteScheme3());
            } else{
                if (stage.getAuthenticationLevel() == 2 && stage.getSensitivityLevel() == 1)
                    stage.setScheme(ExampleBYOD.onsiteScheme1());
                else if (stage.getAuthenticationLevel() == 2)stage.setScheme(ExampleBYOD.onsiteScheme2());
                else stage.setScheme(ExampleBYOD.onsiteScheme3());
            }
        }
        setModel(model);
    }
}
