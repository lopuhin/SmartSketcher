package com.lopuhin.smartsketcher;

import android.graphics.PointF;


class IncCurve extends AbstractCurve {
    /** 
     * A curve that is built incrementaly (tracks finger motion).
     * Buffers are allocated at initialization, and then new points are added
     * one by one
     */
    private PointF lastPoint;

    IncCurve(int maxPoints) {
        setThickness(0.0f);
        thinPointsBuffer = OpenGLRenderer.allocateBuffer(maxPoints);
        thinBufferSize = 0;
        thickPointsBuffer = OpenGLRenderer.allocateBuffer(maxPoints * 2 + 2);
        thickBufferSize = 0;
        lastPoint = null;
    }
    
    public void addPoint(final PointF point) {
        OpenGLRenderer.addToBuffer(
                thinPointsBuffer, thinBufferSize, new PointF[]{point}, DEFAULT_COLOR);
        thinBufferSize += 1; 
        if (lastPoint != null && hasThickness()) {
            final PointF conn = Vector.sub(point, lastPoint);
            final PointF orth = Vector.normalized(Vector.orth(conn), thickness / 2.0f);
            PointF[] boundary = null;
            int index = 0;
            if (thickBufferSize == 0) {
                // add lastPoint first
                boundary = new PointF[4];
                boundary[0] = Vector.add(lastPoint, orth);
                boundary[1] = Vector.sub(lastPoint, orth);
                index = 2;
            } else {
                boundary = new PointF[2];
            }
            boundary[index + 0] = Vector.add(point, orth);
            boundary[index + 1] = Vector.sub(point, orth);
            OpenGLRenderer.addToBuffer(
                    thickPointsBuffer, thickBufferSize, boundary, DEFAULT_COLOR);
            thickBufferSize += index + 2;
        }
        lastPoint = point;
    }

    public void clear() {
        thinBufferSize = 0;
        thickBufferSize = 0;
        lastPoint = null;
    }

    public PointF[] getPoints() {
        return null;
    }

}
