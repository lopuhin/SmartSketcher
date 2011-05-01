package com.lopuhin.smartsketcher;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;

public class BezierCurveSet extends Shape {
	private ArrayList<BezierCurve> curves;

	private final static String TAG = "BezierCurveSet";
	private final static float maxFittingError = 3.0f;
	private final static float slowSpeedCoef = 0.2f;
	
	BezierCurveSet(
			final ArrayList<PointF> pointsList, final ArrayList<Long> pointsTimes, 
			final Sheet sheet) {
		curves = new ArrayList<BezierCurve>();
		ArrayList<Integer> splitCurveIndices = splitCurveIndices(pointsList, pointsTimes);
		splitCurveIndices.add(pointsList.size() - 1);
		int prevIndex = 0;
		for (int index: splitCurveIndices) {
			for (BezierCurve c: recursiveSplitting(pointsList, prevIndex, index, sheet)) {
				curves.add(c);
			}
			prevIndex = index;
		}
	}
	
	private static ArrayList<BezierCurve> recursiveSplitting(
			ArrayList<PointF> pointsList, int startIndex, int endIndex, Sheet sheet) {
		// return curves, approximating this part of points, splitting recursively
		ArrayList<BezierCurve> curves = new ArrayList<BezierCurve>();
		BezierCurve initialCurve = new BezierCurve(pointsList, startIndex, endIndex, sheet);
		Log.d(TAG, "Fitting from " + startIndex + " to " + endIndex + ": error: " + 
				initialCurve.getFittingError());
		if (endIndex - startIndex > 1 && initialCurve.getFittingError() > maxFittingError) {
			final int midIndex = (endIndex + startIndex) / 2;
			for (BezierCurve c: recursiveSplitting(pointsList, startIndex, midIndex, sheet)) {
				curves.add(c);	
			}
			for (BezierCurve c: recursiveSplitting(pointsList, midIndex, endIndex, sheet)) {
				curves.add(c);	
			}
		} else {
			curves.add(initialCurve);
		}
		return curves;
	}

	private static ArrayList<Integer> splitCurveIndices(
			ArrayList<PointF> pointsList, ArrayList<Long> pointsTimes) {
		// return indices of points that should be the on-curve control points of Bezier curves,
		// not including first and last points of pointsList, based on speed and curvature
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

	@Override
	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		for (BezierCurve c: curves) {
			c.draw(canvas, paint, sheet);
		}
	}

	public void toXml(XmlSerializer s) throws IOException {
		for (BezierCurve curve: curves) {
			curve.toXml(s);
		}
	}
	
	private static float[] getSpeeds(
			final ArrayList<PointF> pointsList, final ArrayList<Long> pointsTimes) {
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

	private static float getAvarage(float[] values) {
		float sum = 0;
		for (float v: values) {
			sum += v;
		}
		return sum / values.length;
	}
	
}
