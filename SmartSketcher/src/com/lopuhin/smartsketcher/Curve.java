package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.nio.FloatBuffer;

import android.util.Log;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;


public class Curve extends Shape {
    /**
     * Simple curve from line segments
     */
    private PointF[] points;
    private FloatBuffer pointsBuffer;
    private int bufferSize;
    private Boolean isTransient;
    private float thickness;
    private int drawMode;

    private final static String TAG = "Curve";
    
    Curve(PointF[] points, Boolean isTransient, float thickness) {
        this.isTransient = isTransient;
        this.points = points;
        this.thickness = thickness;
        initBuffer();
    }

    Curve(ArrayList<PointF> pointsList, Boolean isTransient, float thickness) {
        this.isTransient = isTransient;
        this.thickness = thickness;
        points = pointsList.toArray(new PointF[pointsList.size()]);
        initBuffer();
    }

    private void initBuffer() {
        /**
         * Init buffer - with thickness of without
         */
        PointF[] bufferPoints = null;
        if (hasThickness()) {
            bufferPoints = Vector.createBoundary(points, thickness);
            drawMode = GLES20.GL_TRIANGLE_STRIP;
        }
        if (bufferPoints == null || bufferPoints.length < 4) {
            bufferPoints = points;
            drawMode = GLES20.GL_LINE_STRIP;
        }
        pointsBuffer = OpenGLRenderer.createBuffer(bufferPoints, DEFAULT_COLOR);
        bufferSize = bufferPoints.length;

    }
    
    public void draw(OpenGLRenderer renderer) {
        renderer.drawArray(pointsBuffer, bufferSize, drawMode);
    }
    
    @Override
    public PointF[] getPoints() {
        return points;
    }

    @Override
    public float getThickness() {
        return thickness;
    }
    
    @Override
    public Boolean isTransient() {
        return isTransient;
    }

}
