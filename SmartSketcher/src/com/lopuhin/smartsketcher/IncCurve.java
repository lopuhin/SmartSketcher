package com.lopuhin.smartsketcher;

import android.graphics.PointF;


class IncCurve extends AbstractCurve {
    /** 
     * A curve that is built incrementaly (tracks finger motion).
     * Buffers are allocated at initialization, and then new points are added
     * one by one
     */
    IncCurve(int maxPoints) {
        setThickness(0.0f);
        thinPointsBuffer = OpenGLRenderer.allocateBuffer(maxPoints);
        thinBufferSize = 0;
    }
    
    public void addPoint(final PointF point) {
        OpenGLRenderer.addToBuffer(
                thinPointsBuffer, thinBufferSize, point, DEFAULT_COLOR);
        thinBufferSize += 1; 
    }

    public void clear() {
        thinBufferSize = 0;
    }

    public PointF[] getPoints() {
        return null;
    }

}
