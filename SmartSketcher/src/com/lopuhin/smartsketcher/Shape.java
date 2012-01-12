package com.lopuhin.smartsketcher;

import android.graphics.PointF;


public abstract class Shape {

    public abstract PointF[] getPoints();
    public abstract float getThickness();
    
    public Boolean isTransient() {
    	return false;
    }

    public abstract void draw(OpenGLRenderer renderer);

}
