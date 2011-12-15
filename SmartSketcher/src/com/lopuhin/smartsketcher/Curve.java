package com.lopuhin.smartsketcher;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;


public class Curve extends Shape {
    /**
     * Simple curve from line segments
     */
    private PointF[] points;
    private Boolean isTransient;

    /*
      Curve(ArrayList<PointF> pointsList) {
      points = new PointF[pointsList.size()];
      int i = 0;
      for (PointF p: pointsList) {
      points[i] = p;
      i += 1;
      }
      }*/
        
    Curve(ArrayList<PointF> pointsList, Sheet sheet, Boolean _isTransient) {
        isTransient = _isTransient;
        points = new PointF[pointsList.size()];
        int i = 0;
        for (PointF p: pointsList) {
            points[i] = sheet.toSheet(p);
            i += 1;
        }
    }
        
    public void draw(Canvas canvas, Paint paint, Sheet sheet) {
        PointF prevPoint = null;
        for (PointF p: points) {
            p = sheet.toScreen(p);
            if (Config.DEBUG) {
                final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pointPaint.setColor(Color.RED);
                pointPaint.setStrokeWidth(3.0f);
                canvas.drawPoint(p.x, p.y, pointPaint);
            }
            if (prevPoint != null) {
                canvas.drawLine(prevPoint.x, prevPoint.y, p.x, p.y, paint);
            }
            prevPoint = p;
        }        
    }

    @Override
    public Boolean isTransient() {
        return isTransient;
    }
    
    /*
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

      public static Curve fromXml(Node node) throws IOException {
      ArrayList<PointF> points = new ArrayList<PointF>();
      NodeList pointNodes = node.getChildNodes();
      for (int i = 0; i < pointNodes.getLength(); i++ ) {
      Node pointNode = pointNodes.item(i);
      NamedNodeMap attr = pointNode.getAttributes();
      points.add(new PointF(
      Float.parseFloat(attr.getNamedItem("x").getNodeValue()),
      Float.parseFloat(attr.getNamedItem("y").getNodeValue())));
      }
      return new Curve(points);
      }*/
        
}
