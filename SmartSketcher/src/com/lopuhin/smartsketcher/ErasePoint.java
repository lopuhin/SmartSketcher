package com.lopuhin.smartsketcher;

import java.nio.FloatBuffer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;


public class ErasePoint extends Shape {
    // TODO - should consist of several points, really
    
    private PointF point;
    private FloatBuffer pointBuffer;
    private float radius;
	
    ErasePoint(final PointF p, final float radius) {
        point = new PointF(p.x, p.y);
        this.radius = radius;
        pointBuffer = OpenGLRenderer.createBuffer(new PointF[]{point},
                                                  Sheet.BACKGROUND_COLOR);
    }
	
    public void draw(OpenGLRenderer renderer) {
        renderer.drawPoints(pointBuffer, 1);
    }

    @Override
    public PointF[] getPoints() {
        return new PointF[]{point};
    }

    @Override
    public float getThickness() {
        return radius;
    }
}
