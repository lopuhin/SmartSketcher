package com.lopuhin.smartsketcher;

import android.util.Log;

public class NewtonSolve {
	private static final String TAG = "NewtonSolve";
	
	public final static float minimize(final Fn fn, float initial) {
		// TODO - check for minimum?
		return solve(new Fn() {
			public float value(float x) {
				return fn.derivative(x);
			}},
			initial);
	}
	
	public final static float solve(final Fn fn, final float initial) {
		// TODO - check for convergence
		float x = initial;
		float fnx = fn.value(x);
		float dfdx;
		final float delta = 0.001f;	
		while (Math.abs(fnx) > delta) {
			dfdx = fn.derivative(x);
			Log.d(TAG, "fn(x) = " + fnx + " df/dx = " + dfdx);
			x = x - fnx / dfdx;
			fnx = fn.value(x);
		}
		return x;
	}
}
