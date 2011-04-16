package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class BezierCurve extends Curve {
	
	BezierCurve(ArrayList<PointF> pointsList, Sheet sheet) {
		// TODO - create parameterization here?
		points = new PointF[pointsList.size()];
		int i = 0;
		for (PointF p: pointsList) {
			points[i] = sheet.toSheet(p);
			i += 1;
		}
	}

	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		final int n = points.length - 1;
		// TODO - decide how many steps to use depending on the scale
		PointF prevPoint = null;
		for (float t = 0; t <= 1; t += 0.05) {
			PointF currPoint = new PointF();
			int i = 0;
			for (PointF p: points) {
				float k = binomial(n, i) * (float)(Math.pow(1 - t, n - i) * Math.pow(t, i));
				p = sheet.toScreen(p);
				currPoint.x += k * p.x;
				currPoint.y += k * p.y;
				i += 1;
			}
			if (prevPoint != null) {
				canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, paint);
			}
			prevPoint = currPoint;
		}
		
	}
	
	private static int factorial(int n) {
		int fact = 1;
		for (int i = 2; i <= n; i++) 
			fact *= i;
		return fact;
	}
	
	private static int binomial(int n, int k) {
		return factorial(n) / factorial(k) / factorial(n - k);
	}
}
