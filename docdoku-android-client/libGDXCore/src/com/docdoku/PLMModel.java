package com.docdoku;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;

/**
 * Created by G. BOTTIEAU on 29/07/14.
 *
 * This class is part of POC. its implementation is very minimalist
 */
// TODO Implement a PLMModel or kind of it
public class PLMModel extends Model /*implements Comparable*/ {

    private String  id;
    private String  partIterationId;
    private Matrix4 matrix;
    private String  nativeCADFile;
    private String  localPath;

    public PLMModel(String id, String partIterationId, Matrix4 matrix, String nativeCADFile, String localPath) {
        super();
        this.id = id;
        this.partIterationId = partIterationId;
        this.matrix = matrix;
        this.nativeCADFile = nativeCADFile;
        this.localPath = localPath;
    }


    // TODO ? Make constructor with Part
//    public PLMModel(Part part){
//
//    }

    public String getId() {
        return id;
    }

    public String getPartIterationId() {
        return partIterationId;
    }

    public Matrix4 getMatrix() {
        return matrix;
    }

    public String getNativeCADFile() {
        return nativeCADFile;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getFullCADFilePath() {
        if (this.localPath.endsWith("/"))
            return this.localPath + nativeCADFile;
        else
            return this.localPath + "/" + nativeCADFile;
    }

    @Override
    public String toString() {
        return partIterationId + " / " + id;
    }

//    @Override
//    public int compareTo(Object o) {
//        return (this.nativeCADFile.compareTo(((PLMModel) o).getNativeCADFile()));
//    }
}
