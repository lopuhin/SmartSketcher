package com.lopuhin.smartsketcher;

import java.io.FileOutputStream;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.util.Log;


public class Sheet {
    /**
     * Holds shapes, view position and zoom. Persists itself to database
     */
    public boolean isDirty;
    public Paint paint, whiteFillPaint;
     
    private float viewZoom;  // zoom of the visible screen
    private PointF viewPos; // upper left corner of the visible screen

    private DBAdapter dbAdapter;
    
    private ArrayList<Shape> shapes;
    private ArrayList<IAction> doneActions, undoneActions;
    private final static String TAG = "Sheet";
    
    Sheet(DBAdapter _dbAdapter, Boolean isNew) {
        dbAdapter = _dbAdapter;
        shapes = new ArrayList<Shape>();
        doneActions = new ArrayList<IAction>();
        undoneActions = new ArrayList<IAction>();
        isDirty = true;
        viewZoom = 1.0f;
        viewPos = new PointF(0.0f, 0.0f);
          
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(1.0f);
        whiteFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteFillPaint.setColor(Color.WHITE);
        if (isNew)
            dbAdapter.newSheet(this);
    }

    public DBAdapter getDBAdapter() {
        return dbAdapter;
    }

    public long getId() {
        return dbAdapter.getCurrentSheetId();
    }
    
    public void addShape(final Shape shape, Boolean save) {
        /**
         * Add shape to sheet, save if save is true.
         */
        synchronized (shapes) {
            shapes.add(shape);
        }
        setDirty();
        if (save && !shape.isTransient()) {
            dbAdapter.addShape(shape);
        }
    }

    public void addShape(final Shape shape) {
        addShape(shape, true);
    }
    
    public void removeShape(final Shape shape) {
        synchronized (shapes) {
            // find shape, iterating from the end (shapes are removed by undo)
            for (int i = shapes.size() - 1; i >= 0; i-- ) {
                Shape sh = shapes.get(i);
                if (sh == shape) {
                    shapes.remove(i);
                    setDirty();
                    return;
                }
            }
        }
        setDirty();
        if (!shape.isTransient()) {
            dbAdapter.removeShape(shape);
        }
    }
               
    public void undo() {
        final int lastIdx = doneActions.size() - 1;
        if (lastIdx >= 0) {
            synchronized (shapes) {
                IAction action = doneActions.get(lastIdx);
                undoAction(action);
                synchronized (doneActions) {
                    doneActions.remove(lastIdx);
                }
            }
        }
    }

    public boolean canUndo() {
        return doneActions.size() > 0;
    }
     
    public void redo() {
        final int lastIdx = undoneActions.size() - 1;
        if (lastIdx >= 0) {
            synchronized (shapes) {
                IAction action = undoneActions.get(lastIdx);
                doAction(action);
                synchronized (undoneActions) {
                    undoneActions.remove(lastIdx);
                }
            }
        }
    }

    public boolean canRedo() {
        return undoneActions.size() > 0;
    }
     
    public void doAction(IAction action) {
        synchronized (doneActions) {
            doneActions.add(action);
        }
        action.doAction(this);
    }
     
    public void undoAction(IAction action) {
        synchronized (undoneActions) {
            undoneActions.add(action);
        }
        action.undoAction(this);
    }
     
    public void removeDoneAction(IAction action) {
        synchronized (doneActions) {
            doneActions.remove(action);
        }
    }
     
    public PointF getViewPos() {
        /**
         * Upper left corner of the screen
         * in sheet coordinates
         */
        return new PointF(viewPos.x, viewPos.y);
    }
     
    public void setViewPos(PointF viewPos) {
        this.viewPos = new PointF(viewPos.x, viewPos.y);
        setDirty();
    }
     
    public float getViewZoom() {
        return viewZoom;
    }
     
    public void setViewZoom(float viewZoom) {
        this.viewZoom = viewZoom;
        setDirty();
    }

    public void draw(OpenGLRenderer renderer) {
        /**
         * Draw all shapes on the sheet
         */
        final ArrayList<Shape> shapesCopy;
        synchronized (shapes) {
            shapesCopy = (ArrayList<Shape>) shapes.clone();
        }
        for (Shape sh: shapesCopy) {
            sh.draw(renderer);
        }
    }
    
    public void setDirty() {
        isDirty = true;
    }
     
    public PointF toScreen(PointF p) {
        /**
         * Return Point with the screen coordinates of p
         */
        return new PointF(viewZoom * (p.x - viewPos.x), 
                          viewZoom * (p.y - viewPos.y));
    }
     
    public PointF toSheet(PointF p) {
        /**
         * Return Point with the sheet coordinates of p
         */
        return new PointF(viewPos.x + p.x / viewZoom, 
                          viewPos.y + p.y / viewZoom);
    }

    public float toSheet(float x) {
        /**
         * Return x, scaled to sheet coordinated
         */
        return x / viewZoom;
    }

    public float toScreen(float x) {
        /**
         * Return x, scaled to screen coordinated
         */
        return x * viewZoom;
    }
     
}
