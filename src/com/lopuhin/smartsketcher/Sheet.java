package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class Sheet {

	private ArrayList<Shape> shapes;
	private float viewZoom;  // zoom of the visible screen
	private Point viewPos; // upper left corner of the visible screen
	
	Sheet() {
		shapes = new ArrayList<Shape>();
		viewZoom = 1.0f;
		viewPos = new Point(0, 0);
	}
	
	public void addShape(Shape sh) {
		synchronized (shapes) {
			shapes.add(sh);
		}
	}
	
	public void draw(Canvas canvas, Paint paint) {
		// draw shapes
		synchronized (shapes) {
			for (Shape sh: shapes) {
				sh.draw(canvas, paint, this);
			}	
		}
	}
	
	public Point toScreen(Point p) {
		// return Point with the screen coordinates of p
		return new Point(p.x, p.y);
	}
	
	public Point toSheet(Point p) {
		// return Point with the sheet coordinates of p
		return new Point(p.x, p.y);
	}
	
	
}
