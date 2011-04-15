package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class Curve extends Shape {
	private Point[] points;
	
	Curve(ArrayList<Point> pointsList) {
		points = new Point[pointsList.size()];
		int i = 0;
		for (Point p: pointsList) {
			points[i] = p;
			i += 1;
		}
	}
	
	public void draw(Canvas canvas, Paint paint) {
		Point prevPoint = null;
		for (Point p: points) {
			if (prevPoint != null) {
				canvas.drawLine(prevPoint.x, prevPoint.y, p.x, p.y, paint);
			}
			prevPoint = p;
		}	
	}
	
}
