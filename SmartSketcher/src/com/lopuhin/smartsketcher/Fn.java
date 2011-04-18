package com.lopuhin.smartsketcher;

public abstract class Fn {
	
	public abstract float value(float x);
	
	public float derivative(float x, float dx) {
		return (value(x + dx) - value(x)) / dx;
	}
	
	public float derivative(float x) {
		return derivative(x, 0.001f);
	}
}
