package com.lopuhin.smartsketcher;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;


public class ErasePoint extends Shape {
    private PointF point;
    private float radius;
	
    ErasePoint(final PointF p, final float radius) {
        point = new PointF(p.x, p.y);
        this.radius = radius; 
    }
	
    public void draw(OpenGLRenderer renderer) {
        // TODO
    }

    @Override
    public PointF[] getPoints() {
        PointF[] points = new PointF[1];
        points[0] = point;
        return points;
    }

    @Override
    public float getThickness() {
        return radius;
    }
}
