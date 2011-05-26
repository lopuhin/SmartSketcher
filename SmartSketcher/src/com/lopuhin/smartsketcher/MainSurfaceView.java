package com.lopuhin.smartsketcher;

import java.util.ArrayList;

import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
	
	private float eraserRadius;
	
	private SurfaceHolder holder;
	private MainSurfaceViewThread mainSurfaceViewThread;
	private boolean hasSurface;
	
	private Sheet sheet;
	private ArrayList<PointF> lastSegment;
	private ArrayList<Long> lastSegmentTimes;
	private ArrayList<PointF> lastEraseTrace;
	private int lastSegmentDirtyIndex, lastEraseTraceDirtyIndex;
	private boolean finishErasing;
	
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
		lastSegmentDirtyIndex = 0;
		lastEraseTraceDirtyIndex= -1;
		finishErasing = false;
		//Resources res = getResources();
		//final float eraserRadius = res.getDimension(R.dimen.eraser_radius);
		eraserRadius = 30.0f; // TODO - load from resources
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
			lastSegmentDirtyIndex = 0;
			sheet.doAction(action);			
		}
	}

	private void discardSegment() {
		// lastSegment really was not meant to be drawn 
		synchronized (lastSegment) {
			lastSegment.clear();
			lastSegmentTimes.clear();
			lastSegmentDirtyIndex = 0;
		}
	}
	
	private void finishErasing() {
		// read erasing finishing is done in drawing thread,
		// to erase the outline of eraser
		synchronized (lastEraseTrace) {
			final int size = lastEraseTrace.size();
			if (size > 0) {
				finishErasing = true;
			}
		}
	}
	
	private void discardErasing() {
		synchronized (lastEraseTrace) {
			lastEraseTrace.clear();
			lastEraseTraceDirtyIndex = -1;
		}
	}
	
	class MainSurfaceViewThread extends Thread {
		private boolean done;
		
		Bitmap sheetBitmap;
		Canvas sheetCanvas;
		
		MainSurfaceViewThread() {
			super();
			done = false;
		}
		
		@Override
		public void run() {
			SurfaceHolder surfaceHolder = holder;
			while (!done) {
				boolean needDrawing = sheet.isDirty;
				if (!needDrawing) {
					synchronized (lastSegment) {
						needDrawing = lastSegment.size() > lastSegmentDirtyIndex + 1;
					}
					if (!needDrawing) {
						synchronized (lastEraseTrace) {
							needDrawing = lastEraseTrace.size() > lastEraseTraceDirtyIndex + 1 || finishErasing;
						}
					}
				}
				if (needDrawing) {
					// Lock the surface and return the canvas to draw onto.
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
			// need to redraw entire view here
			if (sheet.isDirty) {
				// draw sheet to bitmap, and post this bitmap on every redraw
				if (sheetCanvas == null) {
					initSheetBuffer(canvas.getWidth(), canvas.getHeight());
				}
				sheet.draw(sheetCanvas);
			}
			canvas.drawBitmap(sheetBitmap, 0, 0, null);
			synchronized (lastSegment) {
				final int size = lastSegment.size(); 
				PointF prevPoint = null, currPoint;
				for (int i = 1; i < size; i++) {
					currPoint = lastSegment.get(i);
					if (prevPoint == null) prevPoint = lastSegment.get(i - 1);
					canvas.drawLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, sheet.paint);
					prevPoint = currPoint;
				}
				lastSegmentDirtyIndex = size - 1;
			}
			synchronized (lastEraseTrace) {
				final int size = lastEraseTrace.size();
				PointF currPoint = null;
				for (int i = 0; i < size; i++) {
					currPoint = lastEraseTrace.get(i);
					canvas.drawCircle(currPoint.x, currPoint.y, eraserRadius, sheet.whiteFillPaint);
				}
				if (currPoint != null) {
					Paint paint;
					if (finishErasing) {
						paint = sheet.whiteFillPaint;
						for (final PointF p: lastEraseTrace) {
							sheet.addShape(new ErasePoint(p, eraserRadius));
						}
						lastEraseTrace.clear();
						lastEraseTraceDirtyIndex = -1;
						finishErasing = false;
					} else {
						paint = sheet.paint;
						lastEraseTraceDirtyIndex = size - 1;
					}
					canvas.drawCircle(currPoint.x, currPoint.y, eraserRadius, paint);
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
			initSheetBuffer(w, h);
		}
		
		private void initSheetBuffer(int w, int h) {
			sheetBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			sheetCanvas = new Canvas(sheetBitmap);
		}
	}
}