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
        pointsBuffer = OpenGLRenderer.createBuffer(points);
    }

    Curve(ArrayList<PointF> pointsList, Boolean _isTransient) {
        isTransient = _isTransient;
        points = new PointF[pointsList.size()];
        int i = 0;
        for (PointF p: pointsList) {
            points[i] = p;
            i += 1;
        }
        pointsBuffer = OpenGLRenderer.createBuffer(points);
    }
    
    public static Curve
        approximated(final ArrayList<PointF> pointsList, Sheet sheet, Boolean isTransient) {
        /**
         * Create curve from sheet points
         */
        PointF[] points = new PointF[pointsList.size()];
        int i = 0;
        for (PointF p: pointsList) {
            points[i] = sheet.toSheet(p);
            i += 1;
        }
        return new Curve(points, isTransient);
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
