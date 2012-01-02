package com.lopuhin.smartsketcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.os.SystemClock;
import android.util.Log;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private FloatBuffer triangleBuffer;

    private int programHandle;
    private int mMVPMatrixHandle, mPositionHandle, mColorHandle;

    private float[] mMVPMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private final int mBytesPerFloat = 4;
    private final int mStrideBytes = 7 * mBytesPerFloat;	
    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;
    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;
    
    private static String TAG = "OpenGLRenderer";

    private final String vertexShaderCode =
        // A constant representing the combined model/view/projection matrix.
        "uniform mat4 u_MVPMatrix;      \n"
        // Per-vertex position information we will pass in.
        + "attribute vec4 a_Position;     \n"
        // Per-vertex color information we will pass in.
        + "attribute vec4 a_Color;        \n"     
        // This will be passed into the fragment shader.
        + "varying vec4 v_Color;          \n"     
        // The entry point for our vertex shader.
        + "void main()                    \n"     
        + "{                              \n"
        // Pass the color through to the fragment shader.
        + "   v_Color = a_Color;          \n"    
        // It will be interpolated across the triangle.
        // gl_Position is a special variable used to store the final position.
        + "   gl_Position = u_MVPMatrix   \n"
        // Multiply the vertex by the matrix to get the final point in
        // normalized screen coordinates.
        + "               * a_Position;   \n"     
        + "}                              \n";    

    private final String fragmentShaderCode =
        // Set the default precision to medium. We don't need as high of a
        "precision mediump float;       \n"
        // precision in the fragment shader.
        // This is the color from the vertex shader interpolated across the
        // triangle per fragment.
        + "varying vec4 v_Color;          \n"     
        // The entry point for our fragment shader.
        + "void main()                    \n"     
        + "{                              \n"
        // Pass the color directly through the pipeline.
        + "   gl_FragColor = v_Color;     \n"    
        + "}                              \n";
    
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing
        // were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mVMatrix, 0,
                          eyeX, eyeY, eyeZ,
                          lookX, lookY, lookZ,
                          upX, upY, upZ);

        // initialize the triangle vertex array
        initShapes();

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        programHandle = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(programHandle, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(programHandle, fragmentShader); // add the fragment shader to program
        // Bind attributes
        GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
        GLES20.glLinkProgram(programHandle);                  // creates OpenGL program executables
        
        // get handle to the vertex shader's a_Position member
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");        

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);        
    }
    
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Log.d(TAG, "width " + width + "height " + height);
        
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
         //Matrix.setLookAtM(mVMatrix, 0,
        //                  0, 0, -3,
        //                  0f, 0f, 0f,
        //                  0f, 1.0f, 0.0f);
    }

    public void onDrawFrame(GL10 unused) {
    
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                 
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);        
        
        triangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                                     GLES20.GL_FLOAT, false,
                                     mStrideBytes, triangleBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        triangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                                     mStrideBytes, triangleBuffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // This multiplies the view matrix by the model matrix,
        // and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mModelMatrix, 0);
        
        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

    }
    
    private int loadShader(int type, String shaderCode) {
    
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type); 
        
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        
        return shader;
    }
    
    private void initShapes() {
    
        float triangleData[] = {
            // X, Y, Z, 
            // R, G, B, A
            -0.5f, -0.25f, 0.0f, 
            1.0f, 0.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.0f, 0.559016994f, 0.0f, 
            0.0f, 1.0f, 0.0f, 1.0f};
        
        // initialize vertex Buffer for triangle  
        ByteBuffer vbb = ByteBuffer.allocateDirect(triangleData.length * mBytesPerFloat); 
        // use the device hardware's native byte order
        // create a floating point buffer from the ByteBuffer
        triangleBuffer = vbb.order(ByteOrder.nativeOrder()).asFloatBuffer();
        // add the coordinates to the FloatBuffer
        // set the buffer to read the first coordinate
        triangleBuffer.put(triangleData).position(0);
    }
    

}
 