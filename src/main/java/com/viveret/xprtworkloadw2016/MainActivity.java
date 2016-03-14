package com.viveret.xprtworkloadw2016;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;

import android.view.Display;
import android.graphics.Point;

import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

import android.widget.FrameLayout;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class MainActivity extends Activity
{
    private GLSurfaceView myGLSurfaceView;
    private MyRenderer myRenderer;
    private List<Scene> mySceneSelection;
    private final String[] myAvailMeshes = {
      "beethoven.ply", "urn2.ply", "ant.ply",
      "walkman.ply", "teapot.ply" };
    private int myMeshAt;

  final String vertexShader =
    "uniform mat4 u_viewMatrix;      \n"
    + "attribute vec4 a_pos;           \n"
    + "attribute vec4 a_color;         \n"
    + "varying vec4 v_color;           \n"
    + "void main()                     \n"
    + "{                               \n"
    + "   v_color = a_color;           \n"
    + "   gl_Position = u_viewMatrix   \n"
    + "                 * a_pos;         \n"
    + "}\n";

  final String fragmentShader =
    "precision mediump float;       \n"
    + "varying vec4 v_color;          \n"
    + "void main()                    \n"
    + "{                              \n"
    + "   gl_FragColor = v_color;     \n"
    + "}\n";

  public void onCreate(Bundle bundle)
  {
    super.onCreate(bundle);
    setContentView(R.layout.main_activity);
    //setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

    // Lets write a graphics workload!
    myMeshAt = 0;
    myGLSurfaceView = new GLSurfaceView(this);
    myGLSurfaceView.setDebugFlags(GLSurfaceView
                                  .DEBUG_CHECK_GL_ERROR);
    //myGLSurfaceView.setBackgroundResource(0);
    myGLSurfaceView.setEGLContextClientVersion(2);
    myRenderer = new MyRenderer();
    myGLSurfaceView.setRenderer(myRenderer);
    ((FrameLayout)findViewById(R.id.glview)).addView(myGLSurfaceView);

      Button btnPrev = (Button) findViewById(R.id.btnPrev);
      if (btnPrev != null) {
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override public void onClick(final View v) {
              myGLSurfaceView.queueEvent(new Runnable() {
                  @Override public void run() {
                    if (myMeshAt > 0) {
                      myMeshAt--;
                      myRenderer.myScene.freeGL();
                      try {
                        myRenderer.myScene = Scene.loadFromFile(
                          MainActivity.this.getAssets().open(
                            MainActivity.this.myAvailMeshes[
                              MainActivity.this.myMeshAt]));
                      } catch (java.io.IOException e) {
                        Log.e(MyApplication.LOGTAG, "Failed to open file " + e);
                      }
                    }
                  }
                });
            }
          });
      }

        Button btnNext = (Button) findViewById(R.id.btnNext);
        if (btnNext != null) {
        btnNext.setOnClickListener(new OnClickListener() {
            @Override public void onClick(final View v) {
              myGLSurfaceView.queueEvent(new Runnable() {
                  @Override public void run() {
                    if (myMeshAt < myAvailMeshes.length - 1) {
                      myMeshAt++;
                      myRenderer.myScene.freeGL();
                      try {
                        myRenderer.myScene = Scene.loadFromFile(
                          MainActivity.this.getAssets().open(
                            MainActivity.this.myAvailMeshes[
                              MainActivity.this.myMeshAt]));
                      } catch (java.io.IOException e) {
                        Log.e(MyApplication.LOGTAG, "Failed to open file " + e);
                      }
                    }
                  }
                });
            }
          });
        }
    }

	@Override
	protected void onResume() {
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		super.onResume();
	    myGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
        myRenderer.onPause();
		myGLSurfaceView.onPause();
	}

    private class MyRenderer implements GLSurfaceView.Renderer {
        public Scene myScene;
        private final float[] mMVPMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];
        private final float[] mModelMatrix = new float[16];
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mTmpMatrix = new float[16];
        private int shaderProgram;
        private int myMatrixViewHandle;
        private int myPosHandle;
        private int myColorHandle;

        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            if (!createShaders()) {
                MainActivity.this.finish();
                return;
            }
            try {
              myScene = Scene.loadFromFile(getAssets().open(
                myAvailMeshes[myMeshAt]));
            } catch (java.io.IOException e) {
              Log.e(MyApplication.LOGTAG, "Failed to open file " + e);
            }
            Matrix.setIdentityM(mProjectionMatrix, 0); // initialize to identity matrix
        }

        public void onDrawFrame(GL10 unused) {
          long time_start = System.nanoTime();
            float trans = ((System.nanoTime() / 50000000) % 360);
            //double trans = ((System.nanoTime() / 10000000) % 1000 - 500) / 1000.f;
            Matrix.setIdentityM(mViewMatrix, 0); // initialize to identity matrix
            Matrix.setIdentityM(mModelMatrix, 0); // initialize to identity matrix
            Matrix.setIdentityM(mTmpMatrix, 0); // initialize to identity matrix

            Matrix.translateM(mModelMatrix, 0, 0, 1f, -8);
             Matrix.setRotateM(mTmpMatrix, 0, -90 + 45, 1, 0, 0);
             Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix.clone(), 0,
                              mTmpMatrix, 0);
            Matrix.setRotateM(mTmpMatrix, 0, trans, 0, 0, 1);
            Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix.clone(), 0,
                              mTmpMatrix, 0);
            //Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -1, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(mTmpMatrix, 0, mViewMatrix, 0,
                              mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0,
                              mTmpMatrix, 0);
            // Redraw background color
            //float c = 1.0f / 256 * ( System.currentTimeMillis() % 256 );
            //GLES20.glClearColor( c, c, c, 1 );
            Scene.checkGLError("frame before");
            GLES20.glUseProgram(shaderProgram);
            Scene.checkGLError("frame use prog");
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT |
                           GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            myMatrixViewHandle = GLES20.glGetUniformLocation(shaderProgram, "u_viewMatrix");
            Scene.checkGLError("frame get uniform");
            myPosHandle = GLES20.glGetAttribLocation(shaderProgram, "a_pos");
            Scene.checkGLError("frame get attrib pos");
            myColorHandle = GLES20.glGetAttribLocation(shaderProgram, "a_color");
            Scene.checkGLError("frame get attrib color");

            GLES20.glUniformMatrix4fv(myMatrixViewHandle, 1, false,
                                      mMVPMatrix, 0);
            Scene.checkGLError("frame uniform matrix");
            myScene.switchTo(unused);
            Scene.checkGLError("frame scene switchto");
            myScene.renderSelf(unused);
            long time_end = System.nanoTime();
            //Log.i(MyApplication.LOGTAG, "time = "
            //      + (time_end - time_start) / 1000000 + "ms");
        }

        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 1000);
            Log.v(MyApplication.LOGTAG, "w = " + width + ", h = "
                  + height + ", r = " + ratio);
        }
      
        private boolean createShaders() {
          int vertexShaderHandle = GLES20.
            glCreateShader(GLES20.GL_VERTEX_SHADER);
          if (vertexShaderHandle == 0) {
            Log.e(MyApplication.LOGTAG, "Failed to make vertex shader");
            return false;
          }

          GLES20.glShaderSource(vertexShaderHandle, vertexShader);
          GLES20.glCompileShader(vertexShaderHandle);
          final int[] status = new int[1];
          GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, status, 0);
          if (status[0] == 0) {
            GLES20.glDeleteShader(vertexShaderHandle);
            Log.e(MyApplication.LOGTAG, "Failed to compile vertex shader");
            return false;
          }

          int fragShaderHandle = GLES20.
            glCreateShader(GLES20.GL_FRAGMENT_SHADER);
          if (fragShaderHandle == 0) {
            Log.e(MyApplication.LOGTAG, "Failed to make fragment shader");
            return false;
          }

          GLES20.glShaderSource(fragShaderHandle, fragmentShader);
          GLES20.glCompileShader(fragShaderHandle);
          GLES20.glGetShaderiv(fragShaderHandle, GLES20.GL_COMPILE_STATUS, status, 0);
          if (status[0] == 0) {
            GLES20.glDeleteShader(fragShaderHandle);
            GLES20.glDeleteShader(vertexShaderHandle);
            Log.e(MyApplication.LOGTAG, "Failed to compile fragment shader");
            return false;
          }

          shaderProgram = GLES20.glCreateProgram();
          if (shaderProgram == 0){
            Log.e(MyApplication.LOGTAG, "Failed to create shader program");
            return false;
          }
          GLES20.glBindAttribLocation(shaderProgram, 0, "a_pos");
          GLES20.glBindAttribLocation(shaderProgram, 1, "a_color");

          GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
          GLES20.glAttachShader(shaderProgram, fragShaderHandle);
          GLES20.glLinkProgram(shaderProgram);

          GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
          if (status[0] == 0) {
            GLES20.glDeleteProgram(shaderProgram);
            GLES20.glDeleteShader(fragShaderHandle);
            GLES20.glDeleteShader(vertexShaderHandle);
            shaderProgram = 0;
            Log.e(MyApplication.LOGTAG, "Failed to link shader program");
            return false;
          }

          return true;
        }

        protected void onPause() {
            myScene.freeGL();
        }
    }
}
