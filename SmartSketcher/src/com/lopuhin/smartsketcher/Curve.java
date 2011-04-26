package com.lopuhin.smartsketcher;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

public class Curve extends Shape {
	protected PointF[] points;
	
	Curve(ArrayList<PointF> pointsList, Sheet sheet) {
		points = new PointF[pointsList.size()];
		int i = 0;
		for (PointF p: pointsList) {
			points[i] = sheet.toSheet(p);
			i += 1;
		}
	}
	
	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		pointPaint.setColor(Color.RED);
		pointPaint.setStrokeWidth(3.0f);
		
		PointF prevPoint = null;
		
		for (PointF p: points) {
			p = sheet.toScreen(p);
			canvas.drawPoint(p.x, p.y, pointPaint);
			if (prevPoint != null) {
				canvas.drawLine(prevPoint.x, prevPoint.y, p.x, p.y, paint);
			}
			prevPoint = p;
		}	
	}
	
	public void toXml(XmlSerializer s) throws IOException {
		s.startTag("", "Curve");
			for (PointF point: points) {
				s.startTag("", "point");
					s.attribute("", "x", String.format("%f", point.x));
					s.attribute("", "y", String.format("%f", point.y));
				s.endTag("", "point");
			}
		s.endTag("", "Curve");
	}

	
}
