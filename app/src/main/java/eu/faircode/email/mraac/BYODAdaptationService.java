package eu.faircode.email.mraac;


import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.AdaptationService;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.MultiStageModel;
import ca.uwaterloo.cs.crysp.libmraacintegration.adaptation.base.Stage;

public class BYODAdaptationService extends AdaptationService {
    private static final String TAG = "BYODMAIN";

    SharedPreferences.Editor editor;
    String[] users = new String[] {
            "171538", "540641", "561993", "657486",
            "693572", "710707", "771782", "863985",
            "218719", "998757"
    };

    String attacker = users[8];
    String host = users[8];

    @Override
    public void onCreate() {
        editor = getSharedPreferences("mraac_debug", MODE_PRIVATE).edit();
        editor.putLong("basetime1", -1);
        editor.putLong("basetime2", -1);
        editor.putLong("basetime3", -1);
        editor.putLong("basetime4", -1);
        editor.putString("basedir", attacker);
        editor.putString("hostdir", host);
        editor.apply();
        super.onCreate();
        Intent i = new Intent(this, BYODAccessService.class);
        i.setAction(BYODAccessService.REQUEST_SENSITIVITY);
        startService(i);
    }

    @Override
    public void initModel() {
        // BYOD Logic
        MultiStageModel model = ExampleBYOD.buildModel();
        System.out.print(model.toCSV());
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
//        MultiStageModel model = ExampleDummy.buildModel();
//        System.out.print(model.toCSV());
//        for (Stage stage: model.getStages()) {
//            if (!stage.isLockedStage()) stage.setScheme((ExampleDummy.exampleAdaptationScheme()));
//            else stage.setScheme((ExampleDummy.emptyAdaptationScheme()));
//        }
//        setModel(model);
    }
}
