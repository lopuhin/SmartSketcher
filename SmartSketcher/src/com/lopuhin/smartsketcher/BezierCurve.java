package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;

public class BezierCurve extends Curve {
	
	private final static String TAG = "BezierCurve";

	BezierCurve(final ArrayList<PointF> pointsList, final Sheet sheet) {
		// creating approximation of pointsList with cubic Bezier curve
		final int nPoints = pointsList.size();
		final PointF p0 = pointsList.get(0);
		final PointF p3 = pointsList.get(nPoints - 1); 
		// TODO translate points to local coordinate system (to calculate fitting error faster)
		final int nTangentPoints = 10; // TODO - choose depending on distance?
		final float tangentNorm = norm(new PointF(p0.x - p3.x, p0.y - p3.y));
		final PointF tangent1 = normalized(
				findTangent(p0, pointsList, 1, nTangentPoints - 1), tangentNorm); 
		final PointF tangent2 = normalized(
				findTangent(p3, pointsList, nPoints - nTangentPoints, nPoints - 2), tangentNorm);
		Log.d(TAG , "tangent1: " + tangent1.x + ", " + tangent1.y);
		Log.d(TAG, "tangent2: " + tangent2.x + ", " + tangent2.y);
		final Fn fitting_fn  = new Fn() {
			public float value(float c) {
				// fitting error - squared max distance from approximating curve to points array
				final PointF p1 = new PointF(p0.x + c * tangent1.x, p0.y + c * tangent1.y);
				final PointF p2 = new PointF(p3.x + c * tangent2.x, p3.y + c * tangent2.y);
				final PointF[] controlPoints = {p0, p1, p2, p3};
				float maxDst2 = 0.0f;
				for (float t = 0; t <= 1; t += 0.1f) {
					final PointF curvePoint = curvePoint(controlPoints, t);
					// TODO - find closest from the pointsList
					// FIXME - slow, but works without coordinate transformation
					float minDst2 = -1.0f;
					for (PointF p: pointsList) {
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
		final float c = Solve.minimizeByStepping(fitting_fn, 0.0f, 3.0f, 0.05f);
		Log.d(TAG, "solution: c = " + c);
		final PointF p1 = new PointF(p0.x + c * tangent1.x, p0.y + c * tangent1.y);
		final PointF p2 = new PointF(p3.x + c * tangent2.x, p3.y + c * tangent2.y);
		points = new PointF[4];
		points[0] = p0; points[1] = p1; points[2] = p2; points[3] = p3;
	}

	private static PointF curvePoint(final PointF[] points, final float t) {
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
	
	private static PointF findTangent(final PointF at, final ArrayList<PointF> points, final int startIndex, final int endIndex) {
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
