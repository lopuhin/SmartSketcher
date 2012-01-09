package com.lopuhin.smartsketcher;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Paint;


public abstract class Shape {

    public abstract PointF[] getPoints();
    public abstract float getThickness();
    
    public Boolean isTransient() {
    	return false;
    }
    
    public abstract void draw(Canvas canvas, Paint paint, Sheet sheet);

    public abstract void draw(OpenGLRenderer renderer);

}
