package com.lopuhin.smartsketcher;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

	/*
	@Override
	public void toXml(XmlSerializer s) throws IOException {
		s.startTag("", "ErasePoint");
			s.attribute("", "x", String.format("%f", point.x));
			s.attribute("", "y", String.format("%f", point.y));
			s.attribute("", "radius", String.format("%f", radius));
		s.endTag("", "ErasePoint");
	}
	
	public static ErasePoint fromXml(Node node) throws IOException {
		NamedNodeMap attr = node.getAttributes();
		return new ErasePoint(
				new PointF(Float.parseFloat(attr.getNamedItem("x").getNodeValue()),
						Float.parseFloat(attr.getNamedItem("y").getNodeValue())),
				Float.parseFloat(attr.getNamedItem("radius").getNodeValue()));
	}
	*/
	
}
