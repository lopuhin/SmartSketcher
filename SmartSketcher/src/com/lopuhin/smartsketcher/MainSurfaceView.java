package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;


public class MainSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback {
	
	private int mode;
	private final static int ZOOM_MODE = 0, DRAW_MODE = 1, IDLE_MODE = 2;
	
	// initial configuration, when user starts dragging with two fingers
	private float prevTouchSpacing, prevViewZoom;
	private PointF prevTouchCenter, prevViewPos;
	
	private SurfaceHolder holder;
	private MainSurfaceViewThread mainSurfaceViewThread;
	private boolean hasSurface;
	
	private Sheet sheet;
	private ArrayList<PointF> lastSegment;
	private ArrayList<Long> lastSegmentTimes;
	
	private final static String TAG = "MainSurfaceView";
	
	MainSurfaceView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		mode = IDLE_MODE;
		// Create a new SurfaceHolder and assign this class as its callback.
		holder = getHolder();
		holder.addCallback(this);
		hasSurface = false;
		sheet = new Sheet();
		lastSegment = new ArrayList<PointF>();
		lastSegmentTimes = new ArrayList<Long>();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mainSurfaceViewThread != null) {
			final PointF mainPoint = new PointF(event.getX(), event.getY());
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case (MotionEvent.ACTION_DOWN) :
				mode = DRAW_MODE;
				addPoint(mainPoint);
				break;
			case (MotionEvent.ACTION_POINTER_DOWN) :
				final float dst = touchSpacing(event); 
				if (dst > 5f) {
					mode = ZOOM_MODE;
					discardSegment();
					prevViewZoom = sheet.viewZoom;
					prevViewPos = new PointF(sheet.viewPos.x, sheet.viewPos.y);
					prevTouchSpacing = dst;
					prevTouchCenter = touchCenter(event);
				}
				break;
			case (MotionEvent.ACTION_MOVE) :
				if (mode == ZOOM_MODE) {
					final float touchSpacing = touchSpacing(event); 
					final float dZoom = touchSpacing / prevTouchSpacing;
					sheet.viewZoom = prevViewZoom * dZoom;
					final PointF touchCenter = touchCenter(event);
					sheet.viewPos = new PointF(
							prevViewPos.x +	(prevTouchCenter.x - touchCenter.x / dZoom) / sheet.viewZoom,
							prevViewPos.y + (prevTouchCenter.y - touchCenter.y / dZoom) / sheet.viewZoom);
					prevViewZoom = sheet.viewZoom;
					prevViewPos = sheet.viewPos;
					prevTouchSpacing = touchSpacing;
					prevTouchCenter = touchCenter;
				} else if (mode == DRAW_MODE) {
					addPoint(mainPoint);
				}
				break;	
			case (MotionEvent.ACTION_UP) :
				if (mode == DRAW_MODE) {
					addPoint(mainPoint);
					finishSegment();
				}
				mode = IDLE_MODE;
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
		if (mainSurfaceViewThread == null) {
			resume();
		} else {
			mainSurfaceViewThread.start();
		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
		pause();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mainSurfaceViewThread != null)	
			mainSurfaceViewThread.onWindowResize(w, h);
	}
	
	private float touchSpacing(MotionEvent event) {
		final float x = event.getX(0) - event.getX(1);
		final float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	
	private PointF touchCenter(MotionEvent event) {
		return new PointF(
			(event.getX(0) + event.getX(1)) / 2,
			(event.getY(0) + event.getY(1)) / 2);
	}
 
	public Sheet getSheet() {
		return sheet;
	}
	
	public void setSheet(Sheet sheet) {
		this.sheet = sheet;
	}

	private void addPoint(PointF p) {
		final long t = System.currentTimeMillis();
		synchronized (lastSegment) {
			lastSegment.add(p);
		}
		synchronized (lastSegmentTimes) {
			lastSegmentTimes.add(t);
		}
	}
	
	private void finishSegment() {
		// TODO - convert lastSegment to Shape in drawing thread, or in a separate thread
		synchronized (lastSegment) { // TODO - do not block for long, just copy
			synchronized (lastSegmentTimes) {
				assert lastSegment.size() == lastSegmentTimes.size();
				sheet.addShape(new Curve(lastSegment, sheet));
				for (BezierCurve curve: BezierCurveSet.approximated(lastSegment, lastSegmentTimes, sheet)) {
					sheet.addShape(curve);
				}
				lastSegment.clear();	
				lastSegmentTimes.clear();
			}
		}
	}

	private void discardSegment() {
		synchronized (lastSegment) {
			lastSegment.clear();
		}
		synchronized (lastSegmentTimes) {
			lastSegmentTimes.clear();
		}
	}

	class MainSurfaceViewThread extends Thread {
		private Paint paint;
		
		private boolean done;
		
		MainSurfaceViewThread() {
			super();
			done = false;
			
			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1.0f);
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
			// clear canvas with white color
			canvas.drawRGB(255, 255, 255);
			// draw all saved shapes
			sheet.draw(canvas, paint);
			// draw last segment
			PointF prevPoint = null;
			synchronized (lastSegment) {
				for (PointF p: lastSegment) {
					if (prevPoint != null) {
						canvas.drawLine(prevPoint.x, prevPoint.y, p.x, p.y, paint);
					}
					prevPoint = p;
				}
			}
		}
		
		public void requestExitAndWait() {
			// Mark this thread as complete and combine into the main application thread.
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