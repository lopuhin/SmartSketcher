package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class BezierCurve extends Curve {
	
	BezierCurve(final ArrayList<PointF> pointsList, final Sheet sheet) {
		// creating approximation of pointsList with cubic Bezier curve
		final PointF p0 = pointsList.get(0);
		final PointF p3 = pointsList.get(pointsList.size() - 1); 
		// TODO translate points to local coordinate system (to calculate fitting error)
		// TODO calculate tangent vectors
		final PointF tangent1 = new PointF(1.0f, -1.0f); 
		final PointF tangent2 = new PointF(-1.0f, -1.0f);
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
		final float c = Solve.minimizeByStepping(fitting_fn, 0.0f, 100.0f, 0.1f);
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
