package com.lopuhin.smartsketcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.os.SystemClock;
import android.util.Log;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.graphics.PointF;
import android.graphics.Bitmap;
import android.graphics.Color;


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
        "  uniform mat4 u_MVPMatrix; \n"
        + "attribute vec4 a_Position; \n"// Per-vertex position information we will pass in.
        + "attribute vec4 a_Color; \n"   // Per-vertex color information we will pass in.
        + "varying vec4 v_Color; \n" // This will be passed into the fragment shader.
        + "void main() \n" // The entry point for our vertex shader.
        + "{ \n"
        + "  v_Color = a_Color; \n" // Pass the color through to the fragment shader.
        // It will be interpolated across the surface.
        // gl_Position is a special variable used to store the final position.
        + "  gl_Position = u_MVPMatrix " // Multiply the vertex by the matrix
        + "    * a_Position; \n" // to get the final point in normalized screen coordinates.
        + "  gl_PointSize = 10.0; \n" // for debugging
        + "} \n";

    private final String fragmentShaderCode =
        // Set the default precision to medium. We don't need as high of a
        // precision in the fragment shader.
        "precision mediump float;       \n"
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
    private boolean screenshot;
    private Bitmap lastScreenshot;
    
    OpenGLRenderer(MainSurfaceView mainSurfaceView) {
        mConfigChooser = new MultisampleConfigChooser();
        this.mainSurfaceView = mainSurfaceView;
        width = mainSurfaceView.getMeasuredWidth();
        height = mainSurfaceView.getMeasuredHeight();
        screenshot = false;
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
        int c = Sheet.BACKGROUND_COLOR;
        GLES20.glClearColor(glColor(Color.red(c)),
                            glColor(Color.green(c)),
                            glColor(Color.blue(c)),
                            glColor(Color.alpha(c)));

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        checkGLError("load vertex shader");
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        checkGLError("load fragment shader");

        // create empty OpenGL Program
        programHandle = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(programHandle, vertexShader);
        checkGLError("attach vertex shader");
        // add the fragment shader to program
        GLES20.glAttachShader(programHandle, fragmentShader);
        checkGLError("attach fragment shader");
        // Bind attributes
        GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
        checkGLError("bind position attribute");
        GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
        checkGLError("bind color attribute");
        // creates OpenGL program executables
        GLES20.glLinkProgram(programHandle);
        checkGLError("link program");
        
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

    public void onDrawFrame(GL10 gl) {
        /**
         * Called on every frame redraw
         */
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        final Sheet sheet = mainSurfaceView.getSheet();
        final PointF ul, lr; // upper left and lower right
        ul = sheet.getViewPos();
        lr = sheet.toSheet(new PointF(width, height));
        final float left = ul.x, right = lr.x;
        final float bottom = lr.y, top = ul.y;
        final float near = 50.0f, far = 1000.0f;
        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    
        Matrix.setLookAtM(mVMatrix, 0,
                          0.0f, 0.0f, 100.0f, // eye
                          0.0f, 0.0f, -1.0f,  // look
                          0.0f, 1.0f, 0.0f);  // up
                   
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

        if (screenshot) {
            makeScreenshot(gl);
            screenshot = false;
        }
    }

    public void setScreenshot() {
        screenshot = true;
    }

    public Bitmap getLastScreenshot() {
        return lastScreenshot;
    }
    
    private void makeScreenshot(GL10 gl) {
        int screenshotSize = width * height;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        bb = null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
        pixelsBuffer = null;

        short sBuffer[] = new short[screenshotSize];
        ShortBuffer sb = ShortBuffer.wrap(sBuffer);
        bitmap.copyPixelsToBuffer(sb);

        //Making created bitmap (from OpenGL points) compatible with Android bitmap
        for (int i = 0; i < screenshotSize; ++i) {                  
            short v = sBuffer[i];
            sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
        }
        sb.rewind();
        bitmap.copyPixelsFromBuffer(sb);
        lastScreenshot = bitmap;
    }

    public void drawArray(FloatBuffer buffer, int nPoints, int instrument) {
        /**
         * Draw with specified instrument (GLES20.GL_* constant) from given FloatBuffer
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
        
        GLES20.glDrawArrays(instrument, 0, nPoints);
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

    public static FloatBuffer createBuffer(PointF[] points, int color) {
        /**
         * Create FloatBuffer for drawing points with specified color
         */
        float data[] = new float[points.length * 7];
        for (int pi = 0; pi < points.length; pi++) {
            PointF p = points[pi];
            int i = pi * 7;
            data[i+0] = p.x;  // x
            data[i+1] = p.y;  // y
            data[i+2] = 0.0f; // z
            data[i+3] = glColor(Color.red(color));
            data[i+4] = glColor(Color.green(color));
            data[i+5] = glColor(Color.blue(color));
            data[i+6] = glColor(Color.alpha(color));
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

    private static float glColor(int color) {
        return color / 255.0f;
    }

    private void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            String errorText = op + ": glError " + error;
            Log.e(TAG, errorText);
            throw new RuntimeException(errorText);
        }
    }
}
 
