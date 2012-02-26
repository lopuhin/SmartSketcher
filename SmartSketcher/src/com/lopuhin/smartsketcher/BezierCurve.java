package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.opengl.GLES20;


public class BezierCurve extends AbstractCurve {
    /**
     * Bezier (smoothed) curve
     */
    private PointF[] points;

    private final static String TAG = "BezierCurve";
    private float fittingError;
    
    BezierCurve(final ArrayList<PointF> pointsList, float thickness) {
        points = new PointF[pointsList.size()];
        int i = 0;
        for (PointF p: pointsList) {
            points[i] = p;
            i += 1;
        }
        setThickness(thickness);
        initBuffer(toSegments());
    }
        
    BezierCurve(final PointF[] pointsList, float thickness) {
        points = pointsList;
        setThickness(thickness);
        initBuffer(toSegments());
    }

    public static BezierCurve approximated(final ArrayList<PointF> pointsList,
                                           float thickness) {
        /**
         * Creating approximation of pointsList with cubic Bezier curve
         */
        return approximated(pointsList, 0, pointsList.size() - 1, thickness);
    }
        
    public static BezierCurve
        approximated(final ArrayList<PointF> pointsList,
                     final int startIndex, final int endIndex, float thickness) {
        /**
         * Creating approximation of pointsList from startIndex to endIndex
         * with cubic Bezier curve
        */
        final PointF[] p = new PointF[4];
        p[0] = pointsList.get(startIndex);
        p[3] = pointsList.get(endIndex);
        final PointF[] tangents = findTangents(p[0], p[3], startIndex, endIndex, pointsList);
        final Fn fitting_fn  = getFittingFn(p[0], p[3], tangents,
                                            startIndex, endIndex, pointsList);
        final float c = Solve.minimizeByStepping(fitting_fn, 0.0f, 1.0f, 0.05f);
        Log.d(TAG, "solution: c = " + c);
        p[1] = new PointF(p[0].x + c * tangents[0].x, p[0].y + c * tangents[0].y);
        p[2] = new PointF(p[3].x + c * tangents[1].x, p[3].y + c * tangents[1].y);
        BezierCurve curve = new BezierCurve(p, thickness);
        curve.fittingError = fitting_fn.value(c); // TODO - normalize by length?
        return curve;
    }

    @Override
    public PointF[] getPoints() {
        return points;
    }
    
    public float getFittingError() {
        // normalized fitting error
        return fittingError;
    }

    private PointF[] toSegments() {
        /**
         * Return an approximasion of a curve as a series of points
         */
        int nSegments = 20;
        PointF[] segPoints = new PointF[nSegments];
        float t = 0.0f, delta = 1.0f / (nSegments - 1);
        for (int i = 0; i < nSegments - 1; i++, t += delta) {
            segPoints[i] = curvePoint(points, t);
        }
        segPoints[nSegments - 1] = curvePoint(points, 1.0f);
        return segPoints;
    }

    private static Fn getFittingFn(final PointF p0, final PointF p3, final PointF[] tangents,
                                   final int startIndex, final int endIndex,
                                   final ArrayList<PointF> pointsList) {
        // function that measures fitting error of given approximation,
        // using maximum squared distance from curve to path points
                
        // Translating all to coordinate system, where p0 and p3 lie on x-axis
        final PointF shift = new PointF(-p0.x, -p0.y);
        final PointF zero = new PointF();
        final float d = Vector.norm(new PointF(p0.x - p3.x, p0.y - p3.y));
        final float cos = (p3.x - p0.x) / d;
        final float sin = (p3.y - p0.y) / d;
        final PointF[] trTangents = new PointF[]{
            Vector.translated(tangents[0], zero, cos, sin),
            Vector.translated(tangents[1], zero, cos, sin)};
        final PointF trP0 = Vector.translated(p0, shift, cos, sin);
        final PointF trP3 = Vector.translated(p3, shift, cos, sin);
        final ArrayList<PointF> trPoints = new ArrayList<PointF>();
        for (PointF p: pointsList) {
            trPoints.add(Vector.translated(p, shift, cos, sin));
        }
        return new Fn() {
            public float value(final float c) {
                // fitting error - squared max distance of approximating curve to points array
                final PointF trP1 = new PointF(trP0.x + c * trTangents[0].x,
                                               trP0.y + c * trTangents[0].y);
                final PointF trP2 = new PointF(trP3.x + c * trTangents[1].x,
                                               trP3.y + c * trTangents[1].y);
                final PointF[] controlPoints = {trP0, trP1, trP2, trP3};
                float maxDst = 0.0f;
                int closestIndex = 1;
                for (float curveT = 0; curveT <= 1.01; curveT += 0.1f) {
                    final PointF curvePoint = curvePoint(controlPoints, curveT);
                    // find two closest points by x-axis, and approximate y value linearly
                    for (int i = closestIndex; i <= endIndex; i++ ) {
                        final PointF p = trPoints.get(i);
                        if (p.x > curvePoint.x) {
                            final PointF prevP = trPoints.get(i-1);
                            final float t = (curvePoint.x - prevP.x) / (p.x - prevP.x); 
                            final float minDst =
                                Math.abs(curvePoint.y - (t * p.y + (1 - t) * prevP.y));
                            if (minDst > maxDst) {
                                maxDst = minDst;
                            }
                            closestIndex = i;
                            break;
                        }
                    }
                }
                return maxDst;
            }
        };
    }

    private static PointF[]
        findTangents(final PointF p0, final PointF p3, 
                     final int startIndex, final int endIndex,
                     final ArrayList<PointF> pointsList) {
        // find two tangent vectors - one at p0, another at p3,
        // using points on both sides of control points
        // TODO translate points to local coordinate system
        // (to calculate fitting error faster)
        final int nTangentPoints = 10; // TODO - choose depending on distance!
        final float tangentNorm = Vector.norm(new PointF(p0.x - p3.x, p0.y - p3.y));
        PointF t1 = findTangent(p0, startIndex + 1,
                                Math.min(pointsList.size()- 1, startIndex + nTangentPoints),
                                pointsList);
        if (startIndex > 0) { // use points on the both sides of p0
            final PointF t1Outer = findTangent(p0, Math.max(0, startIndex - nTangentPoints),
                                               startIndex - 1, pointsList);
            t1.x -= t1Outer.x;
            t1.y -= t1Outer.y;
        }
        final PointF tangent1 = Vector.normalized(t1, tangentNorm);
        PointF t2 = findTangent(p3, Math.max(0, endIndex - nTangentPoints),
                                endIndex - 1, pointsList);
        if (endIndex < pointsList.size() - 1) { // use points on the both sides of p3
            final PointF t2Outer =
                findTangent(p3, endIndex + 1,
                            Math.min(pointsList.size() - 1, endIndex + nTangentPoints),
                            pointsList);
            t2.x -= t2Outer.x;
            t2.y -= t2Outer.y;
        }
        final PointF tangent2 = Vector.normalized(t2, tangentNorm);
        Log.d(TAG, "tangent1: " + tangent1.x + ", " + tangent1.y);
        Log.d(TAG, "tangent2: " + tangent2.x + ", " + tangent2.y);
        return new PointF[]{tangent1, tangent2};
    }

    private static PointF
        findTangent(final PointF at, final int startIndex, final int endIndex,
                    final ArrayList<PointF> points) {
        // tangent vector at point at, approximated using points from startIndex to endIndex
        PointF tangent = new PointF();
        for (int i = startIndex; i <= endIndex; i++ ) {
            final PointF p = points.get(i);
            // TODO - less weight for points farther away
            final PointF v = Vector.normalized(new PointF(p.x - at.x, p.y - at.y), 1.0f);
            tangent.x += v.x;
            tangent.y += v.y;
        }
        return Vector.normalized(tangent, 1.0f);
    }
        
    private static PointF curvePoint(final PointF[] points, final float t) {
        // get point on curve
        final int n = points.length - 1;
        PointF point = new PointF();
        int i = 0;
        for (PointF p: points) {
            float k = binomial(n, i) * (float)(Math.pow(1 - t, n - i) * Math.pow(t, i));
            point.x += k * p.x;
            point.y += k * p.y;
            i += 1;
        }
        return point;
    }
        
    private static int factorial(final int n) {
        int fact = 1;
        for (int i = 2; i <= n; i++)
            fact *= i;
        return fact;
    }
        
    private static int binomial(final int n, final int k) {
        return factorial(n) / factorial(k) / factorial(n - k);
    }
}
