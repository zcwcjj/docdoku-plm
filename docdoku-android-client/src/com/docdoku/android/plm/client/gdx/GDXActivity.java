/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.android.plm.client.gdx;

import android.os.Bundle;
import android.widget.RelativeLayout;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.docdoku.GdxApplication;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.parts.PartActivity;


public class GDXActivity extends AndroidApplication {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.gdx.GDXActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        GdxApplication gdxPLM = new GdxApplication(PartActivity.plmmodels);


        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.activity_gdxactivity, null);
        RelativeLayout gdxViewLayout = (RelativeLayout) layout.findViewById(R.id.gdxView);
        gdxViewLayout.addView(initializeForView(gdxPLM, config));
        setContentView(layout);

    }


    // TODO implement JSON Processing from POC (needed for assembling parts)
    // This is copy paste from poc !
//    private void processJSON() {
//        try {
//            Array<PLMModel> models = new Array<>(AndroidLauncher.jsonArray.length());
//            int totalModels = 0;
//            for (int i = 0; i < AndroidLauncher.jsonArray.length(); ++i) {
//                JSONObject jsonobject = AndroidLauncher.jsonArray.getJSONObject(i);
//
//                String strFloats = jsonobject.get("matrix").toString().substring(1, jsonobject.get("matrix").toString().length() - 1);
//                String[] splitedStrFloats = strFloats.split(",");
//
//                float[] floats = new float[splitedStrFloats.length];
//                for (int j = 0; j < splitedStrFloats.length; j++) {
//                    floats[j] = Float.valueOf(splitedStrFloats[j]);
//                }
//
//                String nativeCADFile = (String) jsonobject.get("nativeCADFile");
//                if (totalModels < AndroidLauncher.maxModelToLoad) {
//
//                    totalModels++;
//                    Matrix4 matrix = new Matrix4(floats);
//                    matrix.tra(); // transpose matrix
//
//                    PLMModel plmmodel = new PLMModel((String) jsonobject.get("id"),
//                            (String) jsonobject.get("partIterationId"),
//                            matrix,
//                            nativeCADFile,
//                            getExternalCacheDir().getAbsolutePath());
//
//                    models.add(plmmodel);
//                }
//
//            }
//            AndroidLauncher.log("total nails : " + totalModels);
//
////            models.sort();
//        }
//        catch (Exception e) {
//            Log.d(LOG_TAG, e.getMessage());
//        }
//    }

}