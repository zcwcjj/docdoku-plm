
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

package com.docdoku;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

public class GdxApplication extends ApplicationAdapter implements ApplicationListener {
    private static final String LOG_TAG           = "----***** GDX *****---- : ";
    private static final int    MAX_MERGED_MESHES = 128;
    private final Array<PLMModel>       plmModels;
    private       Camera                camera;
    private       CameraInputController camController;
    private       ModelBatch            modelBatch;
    private       Environment           environment;
    private       boolean               loading;
    private       BitmapFont            fpsFont;
    private       SpriteBatch           fpsBatch;
    //    private       AssetManager          assetManager;
    private       Array<ModelInstance>  instances;


    public GdxApplication(Array<PLMModel> plmModels) {
        this.plmModels = plmModels;
    }

    @Override
    public void create() {
        instances = new Array<>();
        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        camera.position.set(1f, 1f, -100f);
//        camera.lookAt(0, 0, 0);
        camera.near = -1f;
        camera.far = 5000f;
//        camera.update();

        camController = new CameraInputController(camera);
        camController.pinchZoomFactor = 200f;
        Gdx.input.setInputProcessor(camController);

//        fpsFont = new BitmapFont();
//        fpsBatch = new SpriteBatch();

        // stop continuous rendering (rendering will occurs when camera is moving only)
        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();

        loading = true;
    }

    @Override
    public void resize(int width, int height) {
        log("resize : " + width + " / " + height);
    }

    @Override
    public void render() {
        if (loading /*&& assetManager.update()*/)
            doneLoading();

//        log("position : " + camera.position.toString());
//        log("direction : " + camera.direction.toString());
//        log("up : " + camera.up.toString());
//        log("---------------------------------------------------------");

        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();

        //FPS displaying
//        fpsBatch.begin();
//        fpsFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
//        fpsFont.draw(fpsBatch, String.format("FPS: %d", Gdx.graphics.getFramesPerSecond()), 10, Gdx.graphics.getHeight() - 10);
//        fpsBatch.end();
    }

    private void doneLoading() {
        // loader and list of models to load
        ModelLoader loader = new ObjLoader();

        // Contains all loaded OBJ Models (cache to avoid reloading)
        ArrayMap<String, Model> loadedModels = new ArrayMap<>();

        // contains all meshes of all model iterations
        ArrayMap<String, Array<Mesh>> allModelMeshes = new ArrayMap<>();

        Model currentModel = null; // will be used to iterate model meshes
        String lastObj = null;

        for (int i = 0; i < plmModels.size; i++) {
            String objName = plmModels.get(i).getFullCADFilePath();

            if (!loadedModels.containsKey(objName)) {
                // load Obj file and save it to ArrayMap
                loadedModels.put(objName, loader.loadModel(new FileHandle(objName)));

                // init key map
                allModelMeshes.put(objName, new Array<Mesh>());
                currentModel = loadedModels.get(objName);
            }

            // condition to avoid key iteration on ArrayMap
            if (!objName.equals(lastObj)) {
                log(lastObj + " != " + objName);
                lastObj = objName;
                currentModel = loadedModels.get(objName);
            }
            else {
                log(lastObj + " == " + objName);
            }


            for (int j = 0; j < currentModel.meshes.size; j++) {
                Mesh m;
                try { // sometimes copy() throws a NullPointerException about indices...
                    m = currentModel.meshes.get(j).copy(true);
                }
                catch (NullPointerException e) {
                    m = currentModel.meshes.get(j);
                }
                m.transform(plmModels.get(i).getMatrix().cpy());

                //save mesh for next merging
                allModelMeshes.get(objName).add(m);
            }
        }

        ArrayMap.Keys<String> keys = loadedModels.keys();
        do {
            String objName = keys.next();
            Material material = loadedModels.get(objName).materials.first();

            //All meshes from current model
            Array<Mesh> meshesArray = allModelMeshes.get(objName);


            // variables for merging
            int limit = 0;
            Array<Mesh> meshesBundle = null;

            for (int i = 0; i < meshesArray.size; i++) {
                limit++;

                if (meshesBundle == null) {
                    meshesBundle = new Array<>();
                }

                // if limit is reached or if it's end of array : merge meshes and create a model
                if (limit == MAX_MERGED_MESHES || i == meshesArray.size - 1) {
                    meshesBundle.add(meshesArray.get(i)); // add current iteration

                    // to merge meshes, have to create an Array of meshes, and give it to static method "Mesh.create"
                    Mesh[] meshA = new Mesh[meshesBundle.size];
                    // TODO remove meshesBundle and for loop : work with Mesh[] directly !
                    for (int j = 0; j < meshesBundle.size; j++) {
                        meshA[j] = meshesBundle.get(j);
                    }
                    Mesh mesh = Mesh.create(true, meshA); // result of merge

                    ModelBuilder modelBuilder = new ModelBuilder();
                    modelBuilder.begin();
                    modelBuilder.part("part", mesh, GL20.GL_TRIANGLES, material);

                    Model m = modelBuilder.end(); // this, create a model

                    instances.add(new ModelInstance(m));

                    // reset values
                    meshesBundle = null;
                    limit = 0;
                }
                else {
                    meshesBundle.add(meshesArray.get(i));
                }
            }

//            Mesh[] meshA = new Mesh[(meshesArray.size) > MAX_MERGED_MESHES ? MAX_MERGED_MESHES : meshesArray.size];
//            for (int i = 0; i < meshesArray.size; i++) {
//                meshA[limit] = meshesArray.get(i);
//
//                limit++;
//
//                // if limit is reached or if it's end of array : merge meshes and create a model
//                if (limit == meshA.length) {
//
//                    // to merge meshes, have to create an Array of meshes, and give it to static method "Mesh.create"
//                    Mesh mesh = Mesh.create(true, meshA); // result of merge
//
//                    ModelBuilder modelBuilder = new ModelBuilder();
//                    modelBuilder.begin();
//                    modelBuilder.part("part", mesh, GL20.GL_TRIANGLES, material);
//
//                    Model m = modelBuilder.end(); // this, create a model
//
//                    instances.add(new ModelInstance(m));
//
//                    // reset values
//                    limit = 0;
//                    meshA = new Mesh[(meshesArray.size - i) > MAX_MERGED_MESHES ? MAX_MERGED_MESHES : meshesArray.size - i];
//                }
//            }
        }
        while (keys.hasNext());
        log("Nb d'instances  : " + instances.size);

        // get boundingbox position of first model.
        BoundingBox boundingBox = new BoundingBox();
        instances.first().calculateBoundingBox(boundingBox);

        log("boundingBox : " + boundingBox);
        log("boundingBox center : " + boundingBox.getCenter());


        //set camera, camera controller's position
        camController.target.set(boundingBox.getCenter().x, boundingBox.getCenter().y, boundingBox.getCenter().z);

        camera.lookAt(boundingBox.getCenter().x, boundingBox.getCenter().y, boundingBox.getCenter().z);
        camera.position.set(boundingBox.getCenter().x, boundingBox.getCenter().y, boundingBox.getCenter().z);
//        camera.up.set(boundingBox.getCenter().x, boundingBox.getCenter().y, boundingBox.getCenter().z);
        camera.update();

        loading = false;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
    }

    private static void log(String str) {
        Gdx.app.log(LOG_TAG, str);
    }
}
