package com.lopuhin.smartsketcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Xml;

public class Sheet {

	private ArrayList<Shape> shapes;
	public float viewZoom;  // zoom of the visible screen
	public PointF viewPos; // upper left corner of the visible screen
	
	Sheet() {
		shapes = new ArrayList<Shape>();
		viewZoom = 1.0f;
		viewPos = new PointF(0.0f, 0.0f);
	}
	
	public static Sheet loadFromFile(FileInputStream fis) {
		return new Sheet(); // TODO
	}
	
	public void saveToFile(FileOutputStream fos) {
		XmlSerializer s = Xml.newSerializer();
		try {
			final String encoding = "UTF-8";
			s.setOutput(fos, encoding);
			s.startDocument(encoding, true);
			s.startTag("", "Sheet");
				s.startTag("", "pos");
					s.attribute("", "x", String.format("%f", viewPos.x));
					s.attribute("", "y", String.format("%f", viewPos.y));
					s.attribute("", "zoom", String.format("%f", viewZoom));
				s.endTag("", "pos");
				for (Shape shape: shapes) {
					shape.toXml(s);
				}
			s.endTag("", "sheet");
			s.endDocument();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 	
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
	
	public PointF toScreen(PointF p) {
		// return Point with the screen coordinates of p
		return new PointF(
				viewZoom * (p.x - viewPos.x), 
				viewZoom * (p.y - viewPos.y));
	}
	
	public PointF toSheet(PointF p) {
		// return Point with the sheet coordinates of p
		return new PointF(
				viewPos.x + p.x / viewZoom, 
				viewPos.y + p.y / viewZoom);
	}
	
	
}
