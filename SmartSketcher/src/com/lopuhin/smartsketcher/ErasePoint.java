package com.lopuhin.smartsketcher;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class ErasePoint extends Shape {
	private PointF point;
	private float radius;
	
	ErasePoint(final PointF p, final float radius) {
		point = new PointF(p.x, p.y);
		this.radius = radius; 
	}
	
	@Override
	public void draw(Canvas canvas, Paint paint, Sheet sheet) {
		canvas.drawCircle(point.x, point.y, radius, sheet.whiteFillPaint);
	}

	@Override
	public void toXml(XmlSerializer serializer) throws IOException {
		// TODO Auto-generated method stub

	}

}
