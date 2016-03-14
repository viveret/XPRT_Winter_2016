package com.viveret.xprtworkloadw2016;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.util.Log;
import java.io.InputStream;
import java.io.IOException;

import org.smurn.jply.*;
import org.smurn.jply.util.*;

public class Mesh {
    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_INT = 4;
    public static final int BYTES_PER_SHORT = 2;

    private static int gNumMeshes = 0;

    public float positions[];
    public float norms[];
    public float colors[];
    public int indices[];

    private int mySize, myIndexIndex, myColorIndex;
    private final FloatBuffer myPosBuf;
    private final FloatBuffer myColorBuf;
    private final IntBuffer myIndexBuffer;
    private final int myMeshId;
    private boolean myDataHasChanged;

    // 0 = vert, 1 = col, 2 = indx
    private final int bufs[] = new int[3];
    //    private static boolean gHasInitialized = false;

    private long perfStartTime, perfDuration;

    public Mesh(int vertCapacity, int numIndices) {
        mySize = 0;
        myIndexIndex = 0;
        myColorIndex = 0;
        perfStartTime = System.nanoTime();
        perfDuration = 0;
        GLES20.glGenBuffers(bufs.length, bufs, 0);

        positions = new float[vertCapacity * 3];
        norms = new float[vertCapacity * 3];
        colors = new float[vertCapacity * 4];
        indices = new int[numIndices];
        myPosBuf = ByteBuffer.allocateDirect(positions.length *
                                               BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        myColorBuf = ByteBuffer.allocateDirect(colors.length *
                                               BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        myIndexBuffer = ByteBuffer.allocateDirect(indices.length *
                                               BYTES_PER_INT)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

        myMeshId = gNumMeshes++ - 1;

        dataHasChanged();
        // Position
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufs[0]);
        checkGLError("bind 0");
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, myPosBuf.capacity() * BYTES_PER_FLOAT,
                            myPosBuf, GLES20.GL_DYNAMIC_DRAW);
        checkGLError("buffer data array buf");
        // Color
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufs[1]);
        checkGLError("bind 1");
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, myColorBuf
                            .capacity() * BYTES_PER_FLOAT,
                            myColorBuf, GLES20.GL_DYNAMIC_DRAW);
        checkGLError("buffer color data array buf");
        // Indices
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufs[2]);
        checkGLError("bind 2");
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
                            myIndexBuffer.capacity()
                            * BYTES_PER_INT, myIndexBuffer,
                            GLES20.GL_DYNAMIC_DRAW);
        checkGLError("buffer data element array");
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        checkGLError("bind array buf 0");
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        checkGLError("bind element array buf 0");
        perfDuration += System.nanoTime() - perfStartTime;
        perfStartTime = 0;
    }

    public int addVert(float[] axes) {
        for (int i = 0; i < axes.length; i++) {
            positions[mySize * 3 + i] = axes[i];
        }
        return mySize++ - 1;
    }

    public int addVert(float x, float y, float z) {
        return addVert(new float[]{x, y, z});
    }

    public void addIndex(int i) {
        indices[myIndexIndex] = i;
        myIndexIndex++;
    }

    public void addIndex(int[] ar) {
        for (int i : ar)
            addIndex(i);
    }

    public int addColor(float[] axes) {
        for (int i = 0; i < axes.length; i++) {
            colors[myColorIndex * 4 + i] = axes[i];
        }
        return myColorIndex++ - 1;
    }

    public int addColor(float r, float g, float b, float a) {
        return addColor(new float[]{r, g, b, a});
    }

    public int getSize() {
        return mySize;
    }

    public void dataHasChanged() {
        myPosBuf.put(positions).position(0);
        myIndexBuffer.put(indices).position(0);
        myColorBuf.put(colors).position(0);
        myDataHasChanged = true;
    }

    public void renderSelf(GL10 unused) {
        checkGLError("before");
        // Position
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufs[0]);
        if (myDataHasChanged) {
            GLES20.glBufferSubData​(GLES20.GL_ARRAY_BUFFER, 0,
                                   myPosBuf.capacity()
                                   * BYTES_PER_FLOAT, myPosBuf);
        }
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false,
                                     0, 0);
        GLES20.glEnableVertexAttribArray(0);

        // Color
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufs[1]);
        if (myDataHasChanged) {
            GLES20.glBufferSubData​(GLES20.GL_ARRAY_BUFFER, 0,
                                   myColorBuf.capacity()
                                   * BYTES_PER_FLOAT, myColorBuf);
        }
        GLES20.glVertexAttribPointer(1, 4, GLES20.GL_FLOAT, false,
                                     0, 0);
        GLES20.glEnableVertexAttribArray(1);
            
        // Indices
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufs[2]);
        if (myDataHasChanged) {
            GLES20.glBufferSubData​(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0,
                                   myIndexBuffer.capacity()
                                   * BYTES_PER_INT, myIndexBuffer);
        }
        
        // Render
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mySize);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, myIndexBuffer
                              .capacity(), GLES20.GL_UNSIGNED_INT,
                              0);
        GLES20.glDisableVertexAttribArray(0);
        GLES20.glDisableVertexAttribArray(1);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        myDataHasChanged = false;
    }

    public static Mesh loadFromFile(final InputStream is) {
        try {
            final PlyReader ply = new NormalizingPlyReader(
                new PlyReaderFile(is),
                TesselationMode.TRIANGLES,
                NormalMode.ADD_NORMALS_CCW,
                TextureMode.XY);
            final Mesh r = new Mesh(ply.getElementCount("vertex"),
                                    ply.getElementCount("face") * 3);

            ElementReader reader = ply.nextElementReader();
            while (reader != null) {
                if (reader.getElementType().getName().equals("vertex")) {
                    Element vertex = reader.readElement();
                    while (vertex != null) {
                        float x = (float) vertex.getDouble("x");
                        float y = (float) vertex.getDouble("y");
                        float z = (float) vertex.getDouble("z");
                        r.addColor(x, y, z, 1);
                        //double nx = vertex.getDouble("nx");
                        //double ny = vertex.getDouble("ny");
                        //double nz = vertex.getDouble("nz");
                        /*vertexBuffer.put((float) nx);
                          vertexBuffer.put((float) ny);
                          vertexBuffer.put((float) nz);*/
                        r.addVert(x, y, z);
                        vertex = reader.readElement();
                    }
                } else if (reader.getElementType().getName().equals("face")) {
                    Element triangle = reader.readElement();
                    while (triangle != null) {
                        int[] indices = triangle.getIntList("vertex_index");
                        for (int index : indices) {
                            r.addIndex(index);
                        }
                        triangle = reader.readElement();
                    }
                }
                reader = ply.nextElementReader();
            }
            r.dataHasChanged();
            Log.v(MyApplication.LOGTAG, "is = " + r.myIndexIndex
                  + "/" + r.myIndexBuffer.capacity());
            return r;
        } catch (IOException e) {
            Log.e(MyApplication.LOGTAG, "Failed to load mesh: " + e);
        }
        return null;
    }

    public static Mesh genCube() {
        // Cube = 8 verts, 6 faces, 12 triangles
        final Mesh r = new Mesh(8, 3 * 12);
        final float[] tlf = {-1, -1,  1};
        final float[] trf = { 1, -1,  1};
        final float[] blf = { 1,  1,  1};
        final float[] brf = {-1,  1,  1};
        final float[] tlb = {-1, -1, -1};
        final float[] trb = { 1, -1, -1};
        final float[] blb = { 1,  1, -1};
        final float[] brb = {-1,  1, -1};
        int tlfi = r.addVert(tlf);
        int trfi = r.addVert(trf);
        int blfi = r.addVert(blf);
        int brfi = r.addVert(brf);
        int tlbi = r.addVert(tlb);
        int trbi = r.addVert(trb);
        int blbi = r.addVert(blb);
        int brbi = r.addVert(brb);

        // tlf
        r.colors[ 0] = 0.75f;
        r.colors[ 1] = 0;
        r.colors[ 2] = 0;
        r.colors[ 3] = 1;
        // trf
        r.colors[ 4] = 0;
        r.colors[ 5] = 1;
        r.colors[ 6] = 0;
        r.colors[ 7] = 1;
        // tlf
        r.colors[ 8] = 0;
        r.colors[ 9] = 0;
        r.colors[10] = 1;
        r.colors[11] = 1;
        // tlf
        r.colors[12] = 1;
        r.colors[13] = 1;
        r.colors[14] = 0;
        r.colors[15] = 1;
        // tlf
        r.colors[16] = 0;
        r.colors[17] = 1;
        r.colors[18] = 1;
        r.colors[19] = 1;
        // tlf
        r.colors[20] = 1;
        r.colors[21] = 1;
        r.colors[22] = 1;
        r.colors[23] = 1;
        // tlf
        r.colors[24] = 1;
        r.colors[25] = 0;
        r.colors[26] = 0;
        r.colors[27] = 1;
        // tlf
        r.colors[28] = 1;
        r.colors[29] = 0.5f;
        r.colors[30] = 0.5f;
        r.colors[31] = 1;

        for (int i = 32; i < r.colors.length; i++)
            r.colors[i] = 0.5f;


		r.addIndex(new int[]{0, 1, 2, 2, 3, 0,
				3, 2, 6, 6, 7, 3,
				7, 6, 5, 5, 4, 7,
				4, 0, 3, 3, 7, 4,
				0, 1, 5, 5, 4, 0,
                             1, 5, 6, 6, 2, 1 });
        // 12 triangles
        // front
        /*r.addIndex(0);r.addIndex(1);r.addIndex(2);
        r.addIndex(2);r.addIndex(3);r.addIndex(0);
        // back
        r.addIndex(4);r.addIndex(5);r.addIndex(6);
        r.addIndex(6);r.addIndex(7);r.addIndex(4);
        // left
        r.addIndex(8);r.addIndex(9);r.addIndex(10);
        r.addIndex(10);r.addIndex(11);r.addIndex(8);
        // right
        r.addIndex(brfi);r.addIndex(trfi);r.addIndex(trbi);
        r.addIndex(brfi);r.addIndex(brbi);r.addIndex(trbi);
        // top
        r.addIndex(trfi);r.addIndex(trbi);r.addIndex(tlbi);
        r.addIndex(trfi);r.addIndex(tlfi);r.addIndex(tlbi);
        // bottom
        r.addIndex(brfi);r.addIndex(brbi);r.addIndex(blbi);
        r.addIndex(brfi);r.addIndex(blfi);r.addIndex(blbi);*/
        /*/ front
        r.addIndex(brfi);r.addIndex(brfi);r.addIndex(tlfi);
        r.addIndex(brfi);r.addIndex(blfi);r.addIndex(tlfi);
        // back
        r.addIndex(brbi);r.addIndex(trbi);r.addIndex(tlbi);
        r.addIndex(brbi);r.addIndex(blbi);r.addIndex(tlbi);
        // left
        r.addIndex(blfi);r.addIndex(tlfi);r.addIndex(tlbi);
        r.addIndex(blfi);r.addIndex(blbi);r.addIndex(tlbi);
        // right
        r.addIndex(brfi);r.addIndex(trfi);r.addIndex(trbi);
        r.addIndex(brfi);r.addIndex(brbi);r.addIndex(trbi);
        // top
        r.addIndex(trfi);r.addIndex(trbi);r.addIndex(tlbi);
        r.addIndex(trfi);r.addIndex(tlfi);r.addIndex(tlbi);
        // bottom
        r.addIndex(brfi);r.addIndex(brbi);r.addIndex(blbi);
        r.addIndex(brfi);r.addIndex(blfi);r.addIndex(blbi);
        */
        r.dataHasChanged();
        return r;
    }

    public void translate(float x, float y, float z) {
        for (int i = 0; i < mySize; i += 3) {
            positions[i] += x;
            positions[i+1] += y;
            positions[i+2] += z;
        }
        dataHasChanged();
    }

    public static void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(MyApplication.LOGTAG, op + ": glError " + error);
        }
    }

    public void freeGL() {
        final IntBuffer ib = ByteBuffer.allocateDirect(bufs.length *
                                               BYTES_PER_INT)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();
        GLES20.glDeleteBuffers(ib.capacity(), ib);
    }
}
