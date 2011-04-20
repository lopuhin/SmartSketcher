package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;

public class BezierCurve extends Shape {
	protected PointF[] points;
	
	private final static String TAG = "BezierCurve";

	public void draw(Canvas canvas, Paint paint, final Sheet sheet) {
		// TODO - decide how many steps to use depending on the scale
		PointF prevPoint = null;
		for (float t = 0; t <= 1; t += 0.05) {
			PointF currPoint = curvePoint(points, t);
			if (prevPoint != null) {
				canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paint);
			}
			prevPoint = currPoint;
		}	
	}
	
	BezierCurve(final ArrayList<PointF> pointsList, final Sheet sheet) {
		// creating approximation of pointsList with cubic Bezier curve
		this(pointsList, 0, pointsList.size() - 1, sheet);
	}
	
	BezierCurve(final ArrayList<PointF> pointsList, final int startIndex, final int endIndex, final Sheet sheet) {
		// creating approximation of pointsList from startIndex to endIndex with cubic Bezier curve
		final PointF p0 = pointsList.get(startIndex);
		final PointF p3 = pointsList.get(endIndex);
		final PointF[] tangents = findTangents(p0, p3, startIndex, endIndex, pointsList);
		final Fn fitting_fn  = getFittingFn(p0, p3, tangents, startIndex, endIndex, pointsList);
		final float c = Solve.minimizeByStepping(fitting_fn, 0.0f, 1.0f, 0.05f);
		Log.d(TAG, "solution: c = " + c);
		final PointF p1 = new PointF(p0.x + c * tangents[0].x, p0.y + c * tangents[0].y);
		final PointF p2 = new PointF(p3.x + c * tangents[1].x, p3.y + c * tangents[1].y);
		points = new PointF[]{p0, p1, p2, p3};
	}

	private static Fn getFittingFn(final PointF p0, final PointF p3, final PointF[] tangents,
			final int startIndex, final int endIndex, final ArrayList<PointF> pointsList) {
		// function that measures maximum squared distance from curve to path points
		return new Fn() {
			public float value(float c) {
				// fitting error - squared max distance from approximating curve to points array
				final PointF p1 = new PointF(p0.x + c * tangents[0].x, p0.y + c * tangents[0].y);
				final PointF p2 = new PointF(p3.x + c * tangents[1].x, p3.y + c * tangents[1].y);
				final PointF[] controlPoints = {p0, p1, p2, p3};
				float maxDst2 = 0.0f;
				for (float t = 0; t <= 1; t += 0.1f) {
					final PointF curvePoint = curvePoint(controlPoints, t);
					// TODO - find closest from the pointsList
					// FIXME - slow, but works without coordinate transformation
					float minDst2 = -1.0f;
					for (int i = startIndex; i <= endIndex; i++) {
						final PointF p = pointsList.get(i);
						final float dx = p.x - curvePoint.x, dy = p.y - curvePoint.y;
						final float dst2 = dx * dx + dy * dy;
						if (minDst2 < 0.0f || dst2 < minDst2) {
							minDst2 = dst2;
						}
					}
					if (minDst2 > maxDst2) {
						maxDst2 = minDst2;
					}
				}
				return maxDst2;
			}
		};
	}

	private static PointF[] findTangents(final PointF p0, final PointF p3, 
			final int startIndex, final int endIndex, final ArrayList<PointF> pointsList) {
		// find two tangent vectors - one at p0, another at p3,
		// using points on both sides of control points
		// TODO translate points to local coordinate system (to calculate fitting error faster)
		final int nTangentPoints = 10; // TODO - choose depending on distance!
		final float tangentNorm = norm(new PointF(p0.x - p3.x, p0.y - p3.y));
		PointF t1 = findTangent(p0, startIndex + 1, startIndex + nTangentPoints, pointsList);
		if (startIndex > 0) { // use points on the both sides of p0
			final PointF t1Outer = findTangent(
					p0, Math.max(0, startIndex - nTangentPoints), startIndex - 1, pointsList);
			t1.x -= t1Outer.x;
			t1.y -= t1Outer.y;
		}
		final PointF tangent1 = normalized(t1, tangentNorm);
		PointF t2 = findTangent(p3, endIndex - nTangentPoints, endIndex - 1, pointsList);
		if (endIndex < pointsList.size() - 1) { // use points on the both sides of p3
			final PointF t2Outer = findTangent(
					p3, endIndex + 1, Math.min(pointsList.size() - 1, endIndex + nTangentPoints), pointsList);
			t2.x -= t2Outer.x;
			t2.y -= t2Outer.y;
		}
		final PointF tangent2 = normalized(t2, tangentNorm);
		Log.d(TAG, "tangent1: " + tangent1.x + ", " + tangent1.y);
		Log.d(TAG, "tangent2: " + tangent2.x + ", " + tangent2.y);
		return new PointF[]{tangent1, tangent2};
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
	
	private static PointF findTangent(final PointF at, 
			final int startIndex, final int endIndex, final ArrayList<PointF> points) {
		// tangent vector at point at, approximated using points from startIndex to endIndex
		PointF tangent = new PointF();
		for (int i = startIndex; i <= endIndex; i++ ) {
			final PointF p = points.get(i);
			// TODO - less weight for points farther away
			final PointF v = normalized(new PointF(p.x - at.x, p.y - at.y), 1.0f);
			tangent.x += v.x;
			tangent.y += v.y;
		}
		return normalized(tangent, 1.0f);
	}
	
	private static float norm(final PointF p) {
		return FloatMath.sqrt(p.x * p.x + p.y * p.y);
	}
	
	private static PointF normalized(final PointF p, final float newNorm) {
		final float norm = norm(p);
		if (norm < 0.0001f) {
			return new PointF();
		} else {
			final float coef = newNorm / norm; 
			return new PointF(p.x * coef, p.y * coef);
		}
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
