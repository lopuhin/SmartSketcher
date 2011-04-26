package com.lopuhin.smartsketcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.XMLFormatter;

import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class BezierCurveSet extends Shape {
	private ArrayList<BezierCurve> curves;

	private final static String TAG = "BezierCurveSet";
	
	BezierCurveSet(final ArrayList<PointF> pointsList, final Sheet sheet) {
		curves = new ArrayList<BezierCurve>();
		ArrayList<Integer> splitCurveIndices = splitCurveIndices(pointsList);
		int prevIndex = 0;
		for (int index: splitCurveIndices) {
			curves.add(new BezierCurve(pointsList, prevIndex, index, sheet));
			prevIndex = index;
		}
		curves.add(new BezierCurve(pointsList, prevIndex, pointsList.size() - 1, sheet));
	}
	
	private static ArrayList<Integer> splitCurveIndices(ArrayList<PointF> pointsList) {
		// return indices of points that should be the on-curve control points of Bezier curves,
		// not including first and last points of pointsList
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

	public void toXml(XmlSerializer s) throws IOException {
		for (BezierCurve curve: curves) {
			curve.toXml(s);
		}
	}
	
}
