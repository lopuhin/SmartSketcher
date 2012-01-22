package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.graphics.PointF;
import android.graphics.Color;


public class EraseTrace extends Shape {
    /**
     * A trace of an eraser
     */
    private final PointF[] points;
    private final FloatBuffer pointsBuffer;
    private final FloatBuffer boundaryBuffer;
    private final int boundarySize;
    private final float thickness;
	
    EraseTrace(final ArrayList<PointF> points, final float thickness) {
        this.thickness = thickness;
        this.points = points.toArray(new PointF[points.size()]);
        pointsBuffer = OpenGLRenderer.createBuffer(this.points, Sheet.BACKGROUND_COLOR);
        PointF[] boundary = Vector.createBoundary(this.points, this.thickness);
        boundarySize = boundary.length;
        boundaryBuffer = OpenGLRenderer.createBuffer(boundary, Sheet.BACKGROUND_COLOR);
    }
	
    public void draw(OpenGLRenderer renderer) {
        if (Config.DEBUG) {
            renderer.drawArray(pointsBuffer, points.length, GLES20.GL_LINE_STRIP);
            renderer.drawArray(pointsBuffer, points.length, GLES20.GL_POINTS);
        }
        if (boundarySize >= 3)
            renderer.drawArray(boundaryBuffer, boundarySize, GLES20.GL_TRIANGLE_STRIP);
        // TODO - draw half-circles or one circle (if only one point)
    }

    @Override
    public PointF[] getPoints() {
        return points;
    }

    @Override
    public float getThickness() {
        return thickness;
    }
}
