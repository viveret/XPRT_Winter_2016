package com.viveret.xprtworkloadw2016;

import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.util.Log;
import android.content.res.AssetManager;

public class Scene {
    private Mesh terrain;
    private List<Mesh> meshes;

    private String displayName;
    private String fileName;

    public Scene() {
        meshes = new ArrayList<Mesh>();
        terrain = null;//Mesh.genCube();
        displayName = "Test Scene";
    }

    public static Scene loadFromFile(final InputStream is) {
        Scene r = new Scene();
        r.meshes.add(Mesh.loadFromFile(is));
        return r;
    }

    public void renderSelf(GL10 unused) {
        checkGLError("scene before");
        if (terrain != null)
            terrain.renderSelf(unused);
        checkGLError("scene");
        for (Mesh m : meshes)
            m.renderSelf(unused);
    }

    public void switchTo(GL10 unused) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Mesh getTerrain() {
        return terrain;
    }

    public static void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(MyApplication.LOGTAG, op + ": glError " + error);
        }
    }

    public void freeGL() {
        if (terrain != null)
            terrain.freeGL();
        for (Mesh m : meshes)
            m.freeGL();
    }
}
