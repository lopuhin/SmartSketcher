package com.lopuhin.smartsketcher;

import java.util.ArrayList;

public class AddShapes implements IAction {
	// adding a list of shapes
	private ArrayList<Shape> shapes;
	
	AddShapes(ArrayList<Shape> shapes) {
		this.shapes = new ArrayList<Shape>();
		for (Shape sh: shapes) {
			this.shapes.add(sh);
		}
	}
	
	AddShapes(Shape sh) {
		this.shapes = new ArrayList<Shape>();
		this.shapes.add(sh);
	}
	
	public void doAction(Sheet sheet) {
		for (Shape sh: shapes) {
			sheet.addShape(sh);
		}
	}
	
	public void undoAction(Sheet sheet) {
		for (Shape sh: shapes) {
			sheet.removeShape(sh);
		}
	}
}