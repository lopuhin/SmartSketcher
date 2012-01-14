package com.lopuhin.smartsketcher;

import android.graphics.PointF;
import android.util.FloatMath;


class Vector {
    /**
     * Some vector utils
     */
    
    public static PointF translated(PointF point, PointF shift, float cos, float sin) {
        /**
         * Return @point, moved by vector @shift and
         rotated by angle (given by @cos and @sin) around the origin
         */
        PointF p = new PointF(point.x + shift.x, point.y + shift.y);
        return new PointF(p.x * cos + p.y * sin, -p.x * sin + p.y * cos);
    }

    public static float norm(final PointF p) {
        /**
         * Euclidian norm
         */
        return FloatMath.sqrt(p.x * p.x + p.y * p.y);
    }
        
    public static PointF normalized(final PointF p, final float newNorm) {
        /**
         * Vecor with the same direction as @p and norm @newNorm
         */
        final float norm = norm(p);
        if (norm < 0.0001f) {
            return new PointF();
        } else {
            final float coef = newNorm / norm; 
            return new PointF(p.x * coef, p.y * coef);
        }
    }

    public static PointF sub(final PointF v1, final PointF v2) {
        return new PointF(v1.x - v2.x, v1.y - v2.y);
    }

    public static PointF add(final PointF v1, final PointF v2) {
        return new PointF(v1.x + v2.x, v1.y + v2.y);
    }

    public static PointF orth(final PointF v) {
        return new PointF(-v.y, v.x);
    }

}