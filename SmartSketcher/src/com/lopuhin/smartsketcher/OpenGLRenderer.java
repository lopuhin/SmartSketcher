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
import android.graphics.PointF;


public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private int programHandle;
    private int mMVPMatrixHandle, mPositionHandle, mColorHandle;

    private float[] mMVPMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    private static final int mBytesPerFloat = 4;
    private static final int mStrideBytes = 7 * mBytesPerFloat;	
    private static final int mPositionOffset = 0;
    private static final int mPositionDataSize = 3;
    private static final int mColorOffset = 3;
    private static final int mColorDataSize = 4;

    private MultisampleConfigChooser mConfigChooser;
    
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
        // It will be interpolated across the surface.
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
        // surface per fragment.
        + "varying vec4 v_Color;          \n"     
        // The entry point for our fragment shader.
        + "void main()                    \n"     
        + "{                              \n"
        // Pass the color directly through the pipeline.
        + "   gl_FragColor = v_Color;     \n"    
        + "}                              \n";

    private MainSurfaceView mainSurfaceView;
    private int width, height;
    
    OpenGLRenderer(MainSurfaceView mainSurfaceView) {
        mConfigChooser = new MultisampleConfigChooser();
        this.mainSurfaceView = mainSurfaceView;
        width = 0;
        height = 0;
    }

    public MultisampleConfigChooser getConfigChooser() {
        /**
         * Enable multisampling (antialiasing)
         */
        return mConfigChooser;
    }
    
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        /**
         * Mostly shaders initialization
         */
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL Program
        programHandle = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(programHandle, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(programHandle, fragmentShader);
        // Bind attributes
        GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
        // creates OpenGL program executables
        GLES20.glLinkProgram(programHandle);
        
        // get handle to the vertex shader's a_Position member
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");        

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);        
    }
    
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        Log.d(TAG, "width " + width + " height " + height);
        
    }

    public void onDrawFrame(GL10 unused) {
    
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        final Sheet sheet = mainSurfaceView.getSheet();
        final PointF ul, lr; // upper left and lower right
        ul = sheet.getViewPos();
        lr = sheet.toSheet(new PointF(width, height));
        
        Log.d(TAG, "ul " + ul.x + " " + ul.y + 
              " lr " + lr.x + " " + lr.y);
        
        // Create a new perspective projection matrix
        // final float ratio = (float) width / height;
        final float left = ul.x;
        final float right = lr.x;
        final float bottom = lr.y;
        final float top = ul.y;
        final float near = 50.0f;
        final float far = 1000.0f; // TODO - use ortho

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    
        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 100.0f;

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
                   
        Matrix.setIdentityM(mModelMatrix, 0);
        // This multiplies the view matrix by the model matrix,
        // and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mModelMatrix, 0);
        
        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    
        mainSurfaceView.draw(this);
    }

    public void drawSegments(FloatBuffer buffer, int nPoints) {
        /**
         * Draw segments from given FloatBuffer
         */
        buffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                                     GLES20.GL_FLOAT, false,
                                     mStrideBytes, buffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        buffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                                     mStrideBytes, buffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, nPoints);

    }
    
    private int loadShader(int type, String shaderCode) {
        /**
         * Create a vertex shader type (GLES20.GL_VERTEX_SHADER)
         * or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
         */
        int shader = GLES20.glCreateShader(type); 
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static FloatBuffer createBuffer(PointF[] points) {
        /**
         * Create FloatBuffer for drawing points
         */
        float data[] = new float[points.length * 7];
        for (int pi = 0; pi < points.length; pi++) {
            PointF p = points[pi];
            int i = pi * 7;
            data[i+0] = p.x;  // x
            data[i+1] = p.y;  // y
            data[i+2] = 0.0f; // z
            data[i+3] = 0.0f; // r
            data[i+4] = 0.0f; // g
            data[i+5] = 0.0f; // b
            data[i+6] = 1.0f; // alpha
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(data.length * mBytesPerFloat); 
        // use the device hardware's native byte order
        // create a floating point buffer from the ByteBuffer
        FloatBuffer buffer = vbb.order(ByteOrder.nativeOrder()).asFloatBuffer();
        // add the coordinates to the FloatBuffer
        // set the buffer to read the first coordinate
        buffer.put(data).position(0);
        return buffer;
    }

}
 