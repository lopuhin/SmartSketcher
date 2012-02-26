package com.lopuhin.smartsketcher;

import android.graphics.PointF;
import android.graphics.Color;


public abstract class Shape {

    public abstract PointF[] getPoints();
    public abstract float getThickness();
    
    public Boolean isTransient() {
    	return false;
    }

    public abstract void draw(OpenGLRenderer renderer, float zoom);

    public final static int DEFAULT_COLOR = Color.BLACK;

    public boolean hasThickness() {
        /**
         * Thickness is visible (drawn as line with thickness)
         */
        return (getThickness() > 0.1f);
    }
}
