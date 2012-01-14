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
    private PointF[] points;
    private FloatBuffer pointsBuffer;
    private float thickness;
	
    EraseTrace(final ArrayList<PointF> points, final float thickness) {
        this.points = points.toArray(new PointF[points.size()]);
        this.thickness = thickness;
        pointsBuffer =
            OpenGLRenderer.createBuffer(this.points, Color.RED);
    }
	
    public void draw(OpenGLRenderer renderer) {
        renderer.drawArray(pointsBuffer, points.length, GLES20.GL_LINE_STRIP);
        //renderer.drawArray(pointsBuffer, points.length, GLES20.GL_TRIANGLE_STRIP);
        if (Config.DEBUG)
            renderer.drawArray(pointsBuffer, points.length, GLES20.GL_POINTS);
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
