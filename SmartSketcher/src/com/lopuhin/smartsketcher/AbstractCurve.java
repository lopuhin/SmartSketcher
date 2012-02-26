package com.lopuhin.smartsketcher;

import java.nio.FloatBuffer;

import android.graphics.PointF;
import android.opengl.GLES20;


public abstract class AbstractCurve extends Shape {
    /**
     * Common drawing stuff for all line-based shapes
     */

    private float thickness;
    private FloatBuffer thinPointsBuffer;
    private int thinBufferSize;
    private FloatBuffer thickPointsBuffer;
    private int thickBufferSize;

    public void draw(OpenGLRenderer renderer, float zoom) {
        /**
         * Draw line or triangle strip depending on thickness and zoom
         * (triangle strip looks bad with when zoomed out)
         */
        if (thickPointsBuffer != null && thickBufferSize > 3 && 
                hasThickness() && (getThickness() * zoom > 1.0f)) {
            renderer.drawArray(thickPointsBuffer, thickBufferSize, GLES20.GL_TRIANGLE_STRIP);
        } else {
            renderer.drawArray(thinPointsBuffer, thinBufferSize, GLES20.GL_LINE_STRIP);
        }
    }

    @Override
    public float getThickness() {
        return thickness;
    }
    
    public void setThickness(float thickness) {
        this.thickness = thickness;
    }
        
    protected void initBuffer(final PointF[] points) {
        /**
         * Init one or two buffers - both if curve has thickness, 
         * and only one (the thin one) if the curve has no thickness.
         */
        if (hasThickness()) {
            PointF[] bufferPoints = Vector.createBoundary(points, thickness);
            thickPointsBuffer = OpenGLRenderer.createBuffer(bufferPoints, DEFAULT_COLOR);
            thickBufferSize = bufferPoints.length;
        }
        thinPointsBuffer = OpenGLRenderer.createBuffer(points, DEFAULT_COLOR);
        thinBufferSize = points.length;
    }
    
}
