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
		// split each curve in two equal parts for now
		curves.add(new BezierCurve(pointsList, 0, pointsList.size() / 2, sheet));
		curves.add(new BezierCurve(pointsList, pointsList.size() / 2, pointsList.size() - 1, sheet));
	}
	
	@Override
	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		for (BezierCurve c: curves) {
			c.draw(canvas, paint, sheet);
		}
	}

}
