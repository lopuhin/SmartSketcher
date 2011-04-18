package com.lopuhin.smartsketcher.test;

import com.lopuhin.smartsketcher.Fn;
import com.lopuhin.smartsketcher.NewtonSolve;

import android.test.AndroidTestCase;
import android.util.FloatMath;

public class NewtonSolveTest extends AndroidTestCase {
	public void testSolve() {
		float solution = NewtonSolve.solve(
			new Fn() {
				public float value(float x) {
					return x * x - 2.0f;
				}
			}, 1.0f);
		assertEquals(FloatMath.sqrt(2.0f), solution, 0.01f);
	}

}
