package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;


public class MainSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback {
	
	private SurfaceHolder holder;
	private MainSurfaceViewThread mainSurfaceViewThread;
	private boolean hasSurface;
	
	MainSurfaceView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		// Create a new SurfaceHolder and assign this class as its callback.
		holder = getHolder();
		holder.addCallback(this);
		hasSurface = false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (mainSurfaceViewThread != null) {
			mainSurfaceViewThread.addPoint((int)event.getX(), (int)event.getY());
			switch (action) {
			case (MotionEvent.ACTION_UP) :
				mainSurfaceViewThread.finishSegment();
				break;
			}
		}
		return true;
	}

	public void resume() {
		// Create and start the graphics update thread.
		if (mainSurfaceViewThread == null) {
			mainSurfaceViewThread = new MainSurfaceViewThread();
			if (hasSurface == true)
				mainSurfaceViewThread.start();
		}	
	}
	
	public void pause() {
		// Kill the graphics update thread
		if (mainSurfaceViewThread != null) {
			mainSurfaceViewThread.requestExitAndWait();
			mainSurfaceViewThread = null;
		}
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		hasSurface = true;
		if (mainSurfaceViewThread != null)
			mainSurfaceViewThread.start();
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
		pause();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mainSurfaceViewThread != null)	
			mainSurfaceViewThread.onWindowResize(w, h);
	}
	
	
	class MainSurfaceViewThread extends Thread {
		public Sheet sheet;
		private ArrayList<Point> lastSegment;
		private Paint paint;
		
		private boolean done;
		
		MainSurfaceViewThread() {
			super();
			done = false;
			lastSegment = new ArrayList<Point>();
			sheet = new Sheet();
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.WHITE);
		}
		
		@Override
		public void run() {
			SurfaceHolder surfaceHolder = holder;
			// Repeat the drawing loop until the thread is stopped.
			while (!done) {
				// Lock the surface and return the canvas to draw onto.
				Canvas canvas = surfaceHolder.lockCanvas();
				draw(canvas);
				// Unlock the canvas and render the current image.
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}

		public void draw(Canvas canvas) {
			// clear canvas
			canvas.drawRGB(0, 0, 0);
			sheet.draw(canvas, paint);
			// draw last segment
			Point prevPoint = null;
			synchronized (lastSegment) {
				for (Point p: lastSegment) {
					if (prevPoint != null) {
						canvas.drawLine(prevPoint.x, prevPoint.y, p.x, p.y, paint);
					}
					prevPoint = p;
				}
			}
		}
		
		public void addPoint(int x, int y) {
			synchronized (lastSegment) {
				mainSurfaceViewThread.lastSegment.add(new Point(x, y));
			}
		}
		
		public void finishSegment() {
			synchronized (lastSegment) {
				sheet.addShape(new Curve(lastSegment, sheet));
				lastSegment.clear();
			}
		}
		
		public void requestExitAndWait() {
			// Mark this thread as complete and combine into
			// the main application thread.
			done = true;
			try {
				join();
			} catch (InterruptedException ex) { }
		}
		
		public void onWindowResize(int w, int h) {
			// Deal with a change in the available surface size.
		}
	}
}