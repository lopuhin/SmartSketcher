package com.lopuhin.smartsketcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.util.Xml;

public class Sheet {
	public boolean isDirty;
	
	private float viewZoom;  // zoom of the visible screen
	private PointF viewPos; // upper left corner of the visible screen
	
	private ArrayList<Shape> shapes;
	private final static String TAG = "Sheet";
	
	Sheet() {
		shapes = new ArrayList<Shape>();
		isDirty = true;
		viewZoom = 1.0f;
		viewPos = new PointF(0.0f, 0.0f);
	}
	
	public static Sheet loadFromFile(FileInputStream fis) {
		// load sheet from XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Sheet sheet = new Sheet();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(fis);
            Element root = dom.getDocumentElement();
            NodeList shapeNodes = root.getChildNodes();
            for (int i = 0; i < shapeNodes.getLength(); i++) {
            	Node node = shapeNodes.item(i);
            	String nodeName = node.getNodeName();
            	Log.d(TAG, "loading from node " + nodeName);
            	if (nodeName.equals("pos")) {
            		NamedNodeMap attr = node.getAttributes();
        			sheet.viewPos = new PointF(
        					Float.parseFloat(attr.getNamedItem("x").getNodeValue()),
        					Float.parseFloat(attr.getNamedItem("y").getNodeValue()));
        			sheet.viewZoom = Float.parseFloat(attr.getNamedItem("zoom").getNodeValue());
            	} else if (nodeName.equals("BezierCurve")) {
            		sheet.addShape(BezierCurve.fromXml(node));
            	} else if (nodeName.equals("Curve")) {
            		sheet.addShape(Curve.fromXml(node));
            	}
            }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return sheet;
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
			s.endTag("", "Sheet");
			s.endDocument();
		} catch (Exception e) {
			Log.e(TAG, "error saving sheet", e);
			throw new RuntimeException(e);
		} 	
	}
	
	public void addShape(final Shape sh) {
		synchronized (shapes) {
			shapes.add(sh);
		}
		isDirty = true;
	}
	
	public void removeShape(final Shape shape) {
		synchronized (shapes) {
			// remove shape, iterating from the end
			for (int i = shapes.size() - 1; i >= 0; i-- ) {
				Shape sh = shapes.get(i);
				if (sh == shape) {
					shapes.remove(i);
					return;
				}
			}
		}
		isDirty = true;
	}
		
	public PointF getViewPos() {
		return new PointF(viewPos.x, viewPos.y);
	}
	
	public void setViewPos(PointF viewPos) {
		this.viewPos = new PointF(viewPos.x, viewPos.y);
		isDirty = true;
	}
	
	public float getViewZoom() {
		return viewZoom;
	}
	
	public void setViewZoom(float viewZoom) {
		this.viewZoom = viewZoom;
		isDirty = true;
	}
	
	public void draw(Canvas canvas, final Paint paint) {
		// draw shapes
		synchronized (shapes) {
			for (Shape sh: shapes) {
				sh.draw(canvas, paint, this);
			}	
		}
		isDirty = false;
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
