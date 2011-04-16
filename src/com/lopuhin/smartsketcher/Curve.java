package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class Curve extends Shape {
	private PointF[] points;
	
	Curve(ArrayList<PointF> pointsList, Sheet sheet) {
		points = new PointF[pointsList.size()];
		int i = 0;
		for (PointF p: pointsList) {
			points[i] = sheet.toSheet(p);
			i += 1;
		}
	}
	
	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		PointF prevPoint = null;
		for (PointF p: points) {
			p = sheet.toScreen(p);
			if (prevPoint != null) {
				canvas.drawLine(prevPoint.x, prevPoint.y, p.x, p.y, paint);
			}
			prevPoint = p;
		}	
	}
	
}
