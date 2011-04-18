package com.lopuhin.smartsketcher.test;

import com.lopuhin.smartsketcher.Fn;
import com.lopuhin.smartsketcher.Solve;

import android.test.AndroidTestCase;
import android.util.FloatMath;

public class SolveTest extends AndroidTestCase {
	public void testSolve() {
		float solution = Solve.solve(
			new Fn() {
				public float value(float x) {
					return x * x - 2.0f;
				}
			}, 1.0f);
		assertEquals(FloatMath.sqrt(2.0f), solution, 0.01f);
	}
	
	public void testMinimizeByStepping() {
		float solution = Solve.minimizeByStepping(
				new Fn() {
					public float value(float x) {
						return (x - 2.0f) * (x - 2.0f);
					}
				}, 0.0f, 10.0f, 0.01f);
		assertEquals(2.0f, solution, 0.01f);
	}

}
