package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.nio.FloatBuffer;

import android.util.Log;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;


public class Curve extends AbstractCurve {
    /**
     * Simple curve from line segments
     */
    private PointF[] points;
    private Boolean isTransient;

    private final static String TAG = "Curve";
    
    Curve(PointF[] points, Boolean isTransient, float thickness) {
        this.isTransient = isTransient;
        this.points = points;
        setThickness(thickness);
        initBuffer(this.points);
    }

    Curve(ArrayList<PointF> pointsList, Boolean isTransient, float thickness) {
        this.isTransient = isTransient;
        setThickness(thickness);
        points = pointsList.toArray(new PointF[pointsList.size()]);
        initBuffer(this.points);
    }

    @Override
    public PointF[] getPoints() {
        return points;
    }

    @Override
    public Boolean isTransient() {
        return isTransient;
    }

}
