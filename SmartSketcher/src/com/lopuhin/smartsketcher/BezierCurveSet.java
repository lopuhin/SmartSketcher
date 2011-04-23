package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class BezierCurveSet extends Shape {
	private ArrayList<BezierCurve> curves;

	private final static String TAG = "BezierCurveSet";
	
	BezierCurveSet(final ArrayList<PointF> pointsList, final Sheet sheet) {
		curves = new ArrayList<BezierCurve>();
		ArrayList<Integer> splitCurveIndices = splitCurveInices(pointsList);
		int prevIndex = 0;
		for (int index: splitCurveIndices) {
			curves.add(new BezierCurve(pointsList, prevIndex, index, sheet));
			prevIndex = index;
		}
		curves.add(new BezierCurve(pointsList, prevIndex, pointsList.size() - 1, sheet));
	}
	
	private static ArrayList<Integer> splitCurveInices(ArrayList<PointF> pointsList) {
		// return indices of points that should be the on-curve control points of Bezier curves
		ArrayList<Integer> indices = new ArrayList<Integer>();
		indices.add(pointsList.size() / 2);
		return indices;
	}

	@Override
	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		for (BezierCurve c: curves) {
			c.draw(canvas, paint, sheet);
		}
	}

}
