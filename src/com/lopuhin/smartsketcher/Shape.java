package com.lopuhin.smartsketcher;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public abstract class Shape {
	public abstract void draw(Canvas canvas, Paint paint, Sheet sheet);
	
}
