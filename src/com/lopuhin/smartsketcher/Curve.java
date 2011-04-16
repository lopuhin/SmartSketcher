package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class Curve extends Shape {
	private Point[] points;
	
	Curve(ArrayList<Point> pointsList, Sheet sheet) {
		points = new Point[pointsList.size()];
		int i = 0;
		for (Point p: pointsList) {
			points[i] = sheet.toSheet(p);
			i += 1;
		}
	}
	
	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		Point prevPoint = null;
		for (Point p: points) {
			p = sheet.toScreen(p);
			if (prevPoint != null) {
				canvas.drawLine(prevPoint.x, prevPoint.y, p.x, p.y, paint);
			}
			prevPoint = p;
		}	
	}
	
}
