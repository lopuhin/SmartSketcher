package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;


public class BezierCurveSet {
    private final static String TAG = "BezierCurveSet";
    private final static float maxFittingError = 2.0f;
    private final static float slowSpeedCoef = 0.2f;
    
    public static ArrayList<Shape>
        approximated(final ArrayList<PointF> pointsList,
                     final ArrayList<Long> pointsTimes) {
        ArrayList<Shape> curves = new ArrayList<Shape>();
        ArrayList<Integer> splitCurveIndices = splitCurveIndices(pointsList, pointsTimes);
        splitCurveIndices.add(pointsList.size() - 1);
        int prevIndex = 0;
        for (int index: splitCurveIndices) {
            Log.d(TAG,  "Fitting start");
            for (Shape sh: recursiveSplitting(pointsList, prevIndex, index)) {
                curves.add(sh);
            }
            prevIndex = index;
        }
        return curves;
    }
    
    private static ArrayList<Shape>
        recursiveSplitting(final ArrayList<PointF> pointsList,
                           final int startIndex,
                           final int endIndex) {
        /**
         * Return curves, approximating this part of points, splitting recursively
         */
        ArrayList<Shape> curves = new ArrayList<Shape>();
        BezierCurve initialCurve = BezierCurve.approximated(
            pointsList, startIndex, endIndex);
        Log.d(TAG, "Fitting from " + startIndex + " to " + endIndex + ": error: " + 
              initialCurve.getFittingError());
        if (initialCurve.getFittingError() > maxFittingError) {
            if (endIndex - startIndex > 4) {
                final int midIndex = (endIndex + startIndex) / 2;
                for (Shape sh: recursiveSplitting(pointsList, startIndex, midIndex)) {
                    curves.add(sh);    
                }
                for (Shape sh: recursiveSplitting(pointsList, midIndex, endIndex)) {
                    curves.add(sh);    
                }
            } else { // add curve from segments
                ArrayList<PointF> points = new ArrayList<PointF>();
                for (int i = startIndex; i <= endIndex; i++ ) {
                    points.add(pointsList.get(i));
                }
                curves.add(new Curve(points, false));
            }
        } else {
            curves.add(initialCurve);
        }
        return curves;
    }

    private static ArrayList<Integer>
        splitCurveIndices(final ArrayList<PointF> pointsList,
                          final ArrayList<Long> pointsTimes) {
        /**
         * Return indices of points that should be the on-curve control points of
         * Bezier curves, not including first and last points of pointsList,
         * based on speed and curvature
         */
        ArrayList<Integer> indices = new ArrayList<Integer>();
        // first split based on speed
        final float[] speeds = getSpeeds(pointsList, pointsTimes);
        final float slowSpeed = slowSpeedCoef * getAvarage(speeds);
        float speed;
        int index;
        Integer slowRegStart = null;
        for (int i = 0; i < speeds.length; i++ ) {
            speed = speeds[i];
            if (speed < slowSpeed) {
                if (slowRegStart == null) {
                    slowRegStart = i; 
                }
            } else if (slowRegStart != null) {
                // slow region ended, add point at it's middle
                if (slowRegStart > 0) {
                    index = (i + slowRegStart) / 2;
                    indices.add(index);
                }
                slowRegStart = null;
            }
        }
        return indices;
    }
    
    private static float[]
        getSpeeds(final ArrayList<PointF> pointsList,
                  final ArrayList<Long> pointsTimes) {
        /**
         * Speeds of drawing
         */
        float[] speeds = new float[pointsList.size() - 1];
        float dt, ds, dx, dy;
        PointF p1, p2;
        for(int i = 0; i < speeds.length; i++ ) {
            dt = pointsTimes.get(i + 1) - pointsTimes.get(i);
            p1 = pointsList.get(i); p2 = pointsList.get(i + 1);
            dx = p1.x - p2.x; dy = p1.y - p2.y;
            ds = FloatMath.sqrt(dx*dx + dy*dy);
            if (dt > 0) {
                speeds[i] = ds / dt;
            } else { // should never happen, really 
                speeds[i] = 100; // large number
            }
        }
        return speeds;
    }

    private static float getAvarage(final float[] values) {
        float sum = 0;
        for (float v: values) {
            sum += v;
        }
        return sum / values.length;
    }
    
}
