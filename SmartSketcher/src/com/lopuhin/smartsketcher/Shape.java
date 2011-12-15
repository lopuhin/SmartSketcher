package com.lopuhin.smartsketcher;

//import java.io.IOException;

//import org.w3c.dom.Node;
//import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.graphics.Paint;


public abstract class Shape {
    public abstract void draw(Canvas canvas, Paint paint, Sheet sheet);

    public Boolean isTransient() {
    	return false;
    }
    
    //public abstract void toXml(XmlSerializer serializer) throws IOException;
    
    //public abstract static Shape fromXml(Node node) throws IOException;
}
