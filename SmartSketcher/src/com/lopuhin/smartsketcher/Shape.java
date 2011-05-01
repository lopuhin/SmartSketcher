package com.lopuhin.smartsketcher;

import java.io.IOException;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class Shape {
	public abstract void draw(Canvas canvas, Paint paint, Sheet sheet);

	public abstract void toXml(XmlSerializer serializer) throws IOException;
	
}
