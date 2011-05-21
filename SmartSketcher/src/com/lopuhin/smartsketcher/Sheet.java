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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.util.Xml;

public class Sheet {
	public boolean isDirty;
	public Paint paint;
	
	private float viewZoom;  // zoom of the visible screen
	private PointF viewPos; // upper left corner of the visible screen
	
	private ArrayList<Shape> shapes;
	private ArrayList<IAction> doneActions, undoneActions;
	private final static String TAG = "Sheet";
	
	Sheet() {
		shapes = new ArrayList<Shape>();
		doneActions = new ArrayList<IAction>();
		undoneActions = new ArrayList<IAction>();
		isDirty = true;
		viewZoom = 1.0f;
		viewPos = new PointF(0.0f, 0.0f);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(1.0f);
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
	
	public void savePreviewToFile(
			FileOutputStream fos, final int previewW, final int previewH) {
		// render preview to file, using default viewZoom and viewPos
		final PointF prevViewPos = getViewPos();
		final float prevViewZoom = getViewZoom();
		try {
			Bitmap bitmap = Bitmap.createBitmap(previewW, previewH, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			setViewPos(new PointF(0.0f, 0.0f));
			setViewZoom(1.0f);
			draw(canvas); 
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} catch (Exception e) {
			Log.e(TAG, "error saving preview", e);
			throw new RuntimeException(e);
		} finally {
			setViewPos(prevViewPos);
			setViewZoom(prevViewZoom);
		}
	}
	
	public void addShape(final Shape sh) {
		synchronized (shapes) {
			shapes.add(sh);
		}
		setDirty();
	}
	
	public void removeShape(final Shape shape) {
		synchronized (shapes) {
			// remove shape, iterating from the end
			for (int i = shapes.size() - 1; i >= 0; i-- ) {
				Shape sh = shapes.get(i);
				if (sh == shape) {
					shapes.remove(i);
					setDirty();
					return;
				}
			}
		}
		setDirty();
	}
		
	public void undo() {
		final int lastIdx = doneActions.size() - 1;
		if (lastIdx >= 0) {
			synchronized (shapes) {
				IAction action = doneActions.get(lastIdx);
				undoAction(action);
				synchronized (doneActions) {
					doneActions.remove(lastIdx);
				}
			}
		}
	}

	public boolean canUndo() {
		return doneActions.size() > 0;
	}
	
	public void redo() {
		final int lastIdx = undoneActions.size() - 1;
		if (lastIdx >= 0) {
			synchronized (shapes) {
				IAction action = undoneActions.get(lastIdx);
				doAction(action);
				synchronized (undoneActions) {
					undoneActions.remove(lastIdx);
				}
			}
		}
	}

	public boolean canRedo() {
		return undoneActions.size() > 0;
	}
	
	public void doAction(IAction action) {
		synchronized (doneActions) {
			doneActions.add(action);
		}
		action.doAction(this);
	}
	
	public void undoAction(IAction action) {
		synchronized (undoneActions) {
			undoneActions.add(action);
		}
		action.undoAction(this);
	}
	
	public void removeDoneAction(IAction action) {
		synchronized (doneActions) {
			doneActions.remove(action);
		}
	}
	
	public PointF getViewPos() {
		return new PointF(viewPos.x, viewPos.y);
	}
	
	public void setViewPos(PointF viewPos) {
		this.viewPos = new PointF(viewPos.x, viewPos.y);
		setDirty();
	}
	
	public float getViewZoom() {
		return viewZoom;
	}
	
	public void setViewZoom(float viewZoom) {
		this.viewZoom = viewZoom;
		setDirty();
	}
	
	public void draw(Canvas canvas) {
		canvas.drawRGB(255, 255, 255);
		// draw shapes
		synchronized (shapes) {
			for (Shape sh: shapes) {
				sh.draw(canvas, paint, this);
			}	
		}
		isDirty = false;
	}
	
	public void setDirty() {
		isDirty = true;
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
