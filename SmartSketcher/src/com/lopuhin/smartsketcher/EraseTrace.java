package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.nio.FloatBuffer;

import android.graphics.Canvas;
import android.graphics.Paint;
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
        pointsBuffer = OpenGLRenderer.createBuffer(this.points,
                                                   Color.RED);
    }
	
    public void draw(OpenGLRenderer renderer) {
        renderer.drawSegments(pointsBuffer, points.length);
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
