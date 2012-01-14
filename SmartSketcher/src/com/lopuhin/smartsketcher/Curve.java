package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.nio.FloatBuffer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;


public class Curve extends Shape {
    /**
     * Simple curve from line segments
     */
    private PointF[] points;
    private FloatBuffer pointsBuffer;
    private Boolean isTransient;

    Curve(PointF[] _points, Boolean _isTransient) {
        isTransient = _isTransient;
        points = _points;
        pointsBuffer = OpenGLRenderer.createBuffer(points, DEFAULT_COLOR);
    }

    Curve(ArrayList<PointF> pointsList, Boolean _isTransient) {
        isTransient = _isTransient;
        points = pointsList.toArray(new PointF[pointsList.size()]);
        pointsBuffer = OpenGLRenderer.createBuffer(points, DEFAULT_COLOR);
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
        return 1.0f; // TODO
    }
    
    @Override
    public Boolean isTransient() {
        return isTransient;
    }

}
