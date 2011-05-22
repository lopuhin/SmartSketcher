package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;


public class MainSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback {
	
	private int mode, submode;
	private final static int ZOOM_MODE = 0, DRAW_MODE = 1, IDLE_MODE = 2;
	public final static int DRAW_SUBMODE = 0, ERASE_SUBMODE = 1;
	// initial configuration, when user starts dragging with two fingers
	private float prevTouchSpacing, prevViewZoom;
	private PointF prevTouchCenter, prevViewPos;
	
	private SurfaceHolder holder;
	private MainSurfaceViewThread mainSurfaceViewThread;
	private boolean hasSurface;
	
	private Sheet sheet;
	private ArrayList<PointF> lastSegment;
	private ArrayList<Long> lastSegmentTimes;
	private ArrayList<PointF> lastEraseTrace;
	
	private final static String TAG = "MainSurfaceView";
	
	MainSurfaceView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		mode = IDLE_MODE;
		submode = DRAW_SUBMODE;
		// Create a new SurfaceHolder and assign this class as its callback.
		holder = getHolder();
		holder.addCallback(this);
		hasSurface = false;
		sheet = new Sheet();
		lastSegment = new ArrayList<PointF>();
		lastSegmentTimes = new ArrayList<Long>();
		lastEraseTrace = new ArrayList<PointF>();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mainSurfaceViewThread != null) {
			final PointF mainPoint = new PointF(event.getX(), event.getY());
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case (MotionEvent.ACTION_DOWN) :
				mode = DRAW_MODE;
				if (submode == DRAW_SUBMODE) {
					addPoint(mainPoint);
				} else if (submode == ERASE_SUBMODE) {
					eraseAt(mainPoint);
				}
				break;
			case (MotionEvent.ACTION_POINTER_DOWN) :
				final float dst = touchSpacing(event); 
				if (dst > 5f) {
					mode = ZOOM_MODE;
					if (submode == DRAW_SUBMODE) {
						discardSegment();
					} else if (submode == ERASE_SUBMODE) {
						discardErasing();
					}
					prevViewZoom = sheet.getViewZoom();
					prevViewPos = sheet.getViewPos();
					prevTouchSpacing = dst;
					prevTouchCenter = touchCenter(event);
				}
				break;
			case (MotionEvent.ACTION_MOVE) :
				if (mode == ZOOM_MODE) {
					final float touchSpacing = touchSpacing(event); 
					final float dZoom = touchSpacing / prevTouchSpacing;
					sheet.setViewZoom(prevViewZoom * dZoom);
					final PointF touchCenter = touchCenter(event);
					sheet.setViewPos(new PointF(
							prevViewPos.x +	(prevTouchCenter.x - touchCenter.x / dZoom) / sheet.getViewZoom(),
							prevViewPos.y + (prevTouchCenter.y - touchCenter.y / dZoom) / sheet.getViewZoom()));
					prevViewZoom = sheet.getViewZoom();
					prevViewPos = sheet.getViewPos();
					prevTouchSpacing = touchSpacing;
					prevTouchCenter = touchCenter;
				} else if (mode == DRAW_MODE) {
					if (submode == DRAW_SUBMODE) {
						addPoint(mainPoint);
					} else if (submode == ERASE_SUBMODE) {
						eraseAt(mainPoint);
					}
				}
				break;	
			case (MotionEvent.ACTION_UP) :
				if (mode == DRAW_MODE) {
					if (submode == DRAW_SUBMODE) {
						addPoint(mainPoint);
						finishSegment();
					} else if (submode == ERASE_SUBMODE) {
						eraseAt(mainPoint);
						finishErasing();
					}
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
	
	public int getSubmode() {
		return submode;
	}
	
	public void setSubmode(int submode) {
		this.submode = submode;
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
		if (sheet != null) {
			this.sheet = sheet;
		}
	}
	
	public void clearSheet() {
		this.sheet = new Sheet();
	}

	private void addPoint(PointF p) {
		final long t = System.currentTimeMillis();
		synchronized (lastSegment) {
			lastSegment.add(p);
			lastSegmentTimes.add(t);
		}
	}
	
	private void eraseAt(PointF p) {
		lastEraseTrace.add(p);
	}
	
	private void finishSegment() {
		// user stopped drawing, we should add smoothed lastSegment to sheet
		synchronized (lastSegment) {
			SmoothLastSegment action = new SmoothLastSegment(lastSegment, lastSegmentTimes);
			lastSegment.clear();	
			lastSegmentTimes.clear();
			sheet.doAction(action);			
		}
	}

	private void discardSegment() {
		// lastSegment really was not meant to be drawn 
		synchronized (lastSegment) {
			lastSegment.clear();
			lastSegmentTimes.clear();
		}
	}
	
	private void finishErasing() {
		// TODO - erase permanently, ensure there is no blinking 
		synchronized (lastEraseTrace) {
			lastEraseTrace.clear();
		}
	}
	
	private void discardErasing() {
		synchronized (lastEraseTrace) {
			lastEraseTrace.clear();
		}
	}
	
	class MainSurfaceViewThread extends Thread {
		private boolean done;
		private int lastSegmentDirtyIndex, lastEraseTraceDirtyIndex;
		private Paint whiteFillPaint, blackOutlinePaint;
		
		
		MainSurfaceViewThread() {
			super();
			done = false;
			lastSegmentDirtyIndex = 0;
			lastEraseTraceDirtyIndex= -1;
			whiteFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			whiteFillPaint.setColor(Color.WHITE);
			blackOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			blackOutlinePaint.setColor(Color.BLACK);
			blackOutlinePaint.setStyle(Style.STROKE);
			blackOutlinePaint.setStrokeWidth(1.0f);
		}
		
		@Override
		public void run() {
			SurfaceHolder surfaceHolder = holder;
			while (!done) {
				// Lock the surface and return the canvas to draw onto.
				boolean needDrawing = sheet.isDirty;
				if (!needDrawing) {
					synchronized (lastSegment) {
						needDrawing = lastSegment.size() > 0;
					}
					if (!needDrawing) {
						synchronized (lastEraseTrace) {
							needDrawing = lastEraseTrace.size() > 0;
						}
					}
				}
				if (needDrawing) {
					Canvas canvas = surfaceHolder.lockCanvas();
					draw(canvas);
					surfaceHolder.unlockCanvasAndPost(canvas);
				} else {
					try {
						MainSurfaceViewThread.sleep(50);
					} catch (InterruptedException e) { }
				}
			}
		}

		public void draw(Canvas canvas) {
			synchronized (lastSegment) {
				final int size = lastSegment.size(); 
				if (size > 1) {
					// draw only not drawn part of last segment
					PointF prevPoint = null, currPoint;
					for (int i = Math.max(1, lastSegmentDirtyIndex); i < size; i++) {
						currPoint = lastSegment.get(i);
						if (prevPoint == null) prevPoint = lastSegment.get(i - 1);
						canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, sheet.paint);
						prevPoint = currPoint;
					}
					lastSegmentDirtyIndex = size - 1;
					return;
				} else {
					lastSegmentDirtyIndex = 0;
				}
			}
			synchronized (lastEraseTrace) {
				final int size = lastEraseTrace.size();
				//Resources res = getResources();
				//final float eraserRadius = res.getDimension(R.dimen.eraser_radius);
				final float eraserRadius = 30.0f; // TODO - load from resources 
				final float eps = 2.0f;
				if (size - 1 > lastEraseTraceDirtyIndex) {
					if (size > 1) { // erase last circle
						final PointF prevPoint = lastEraseTrace.get(size - 2);
						Log.d(TAG, "erase prev point " + eraserRadius);
						canvas.drawCircle(prevPoint.x, prevPoint.y, eraserRadius, whiteFillPaint);
					}
					Log.d(TAG, "erase curr point " + eraserRadius);
					final PointF lastPoint = lastEraseTrace.get(size - 1);
					canvas.drawCircle(lastPoint.x, lastPoint.y, eraserRadius - eps, whiteFillPaint);
					canvas.drawCircle(lastPoint.x, lastPoint.y, eraserRadius - eps, blackOutlinePaint);
					lastEraseTraceDirtyIndex = size - 1;
				} 
				if (size > 0) {
					return;
				} else {
					lastEraseTraceDirtyIndex = -1;
				}
			}
			sheet.draw(canvas);
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