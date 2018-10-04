package developer.mobile.com.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import com.facebook.share.internal.ShareConstants;
import java.io.File;
import java.io.FileOutputStream;

public class TouchImageView extends ImageView {
    static final int CLICK = 3;
    static final int DRAG = 1;
    static final int NONE = 0;
    static final int ZOOM = 2;
    static final int ZOOMDRAG = 3;
    static float resRatio;
    BitmapShader bitmapShader;
    BitmapShader brushBitmapShader;
    Path brushPath;
    Canvas canvas;
    Canvas canvasPreview;
    Paint circlePaint;
    Path circlePath;
    boolean coloring = true;
    Context context;
    PointF curr = new PointF();
    int currentImageIndex = NONE;
    boolean draw = false;
    Paint drawPaint;
    Path drawPath;
    Bitmap drawingBitmap;
    Rect dstRect;
    PointF last = new PointF();
    Paint logPaintColor;
    Paint logPaintGray;
    float[] m;
    ScaleGestureDetector mScaleDetector;
    Matrix matrix;
    float maxScale = 5.0f;
    float minScale = 1.0f;
    int mode = NONE;
    int oldMeasuredHeight;
    int oldMeasuredWidth;
    float oldX = 0.0f;
    float oldY = 0.0f;
    boolean onMeasureCalled = false;
    int opacity = 25;
    protected float origHeight;
    protected float origWidth;
    int pCount1 = -1;
    int pCount2 = -1;
    public boolean prViewDefaultPosition;
    Paint previewPaint;
    float radius = 150.0f;
    float saveScale = 1.0f;
    Bitmap splashBitmap;
    PointF start = new PointF();
    Paint tempPaint;
    Bitmap tempPreviewBitmap;
    int viewHeight;
    int viewWidth;
    float x;
    float y;

    private class MyAnimationListener implements AnimationListener {
        private MyAnimationListener() {
        }

        public void onAnimationEnd(Animation animation) {
            if (TouchImageView.this.prViewDefaultPosition) {
                LayoutParams lp = new LayoutParams(MainActivity.prView.getWidth(), MainActivity.prView.getHeight());
                lp.setMargins(TouchImageView.NONE, TouchImageView.NONE, TouchImageView.NONE, TouchImageView.NONE);
                MainActivity.prView.setLayoutParams(lp);
                return;
            }
            lp = new LayoutParams(MainActivity.prView.getWidth(), MainActivity.prView.getHeight());
            lp.setMargins(TouchImageView.NONE, TouchImageView.this.viewHeight - MainActivity.prView.getWidth(), TouchImageView.NONE, TouchImageView.NONE);
            MainActivity.prView.setLayoutParams(lp);
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    private class SaveCanvasLog extends AsyncTask<String, Integer, String> {
        private SaveCanvasLog() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            TouchImageView touchImageView = TouchImageView.this;
            touchImageView.currentImageIndex += TouchImageView.DRAG;
            File file = new File(MainActivity.tempDrawPathFile, "canvasLog" + TouchImageView.this.currentImageIndex + ".jpg");
            if (file.exists()) {
                file.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(file);
                TouchImageView.this.drawingBitmap.compress(CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TouchImageView.this.currentImageIndex > 5) {
                File file2 = new File(MainActivity.tempDrawPathFile, "canvasLog" + (TouchImageView.this.currentImageIndex - 5) + ".jpg");
                if (file2.exists()) {
                    file2.delete();
                }
            }
            return "this string is passed to onPostExecute";
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            MainActivity.prView.setVisibility(4);
            if (TouchImageView.this.mode == TouchImageView.DRAG || TouchImageView.this.mode == TouchImageView.ZOOMDRAG) {
                TouchImageView.this.mode = TouchImageView.ZOOMDRAG;
            } else {
                TouchImageView.this.mode = TouchImageView.ZOOM;
            }
            TouchImageView.this.draw = false;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = TouchImageView.this.saveScale;
            TouchImageView touchImageView = TouchImageView.this;
            touchImageView.saveScale *= mScaleFactor;
            if (TouchImageView.this.saveScale > TouchImageView.this.maxScale) {
                TouchImageView.this.saveScale = TouchImageView.this.maxScale;
                mScaleFactor = TouchImageView.this.maxScale / origScale;
            } else if (TouchImageView.this.saveScale < TouchImageView.this.minScale) {
            }
            if (TouchImageView.this.origWidth * TouchImageView.this.saveScale <= ((float) TouchImageView.this.viewWidth) || TouchImageView.this.origHeight * TouchImageView.this.saveScale <= ((float) TouchImageView.this.viewHeight)) {
                TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor, (float) (TouchImageView.this.viewWidth / TouchImageView.ZOOM), (float) (TouchImageView.this.viewHeight / TouchImageView.ZOOM));
            } else {
                TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            }
            TouchImageView.this.matrix.getValues(TouchImageView.this.m);
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            TouchImageView.this.radius = ((float) (MainActivity.radiusBar.getProgress() + 50)) / TouchImageView.this.saveScale;
            MainActivity.brushView.setShapeRadiusRatio(((float) (MainActivity.radiusBar.getProgress() + 50)) / TouchImageView.this.saveScale);
            TouchImageView.this.updatePreviewPaint();
        }
    }

    public TouchImageView(Context context) {
        super(context);
        this.context = context;
        sharedConstructing(context);
        this.prViewDefaultPosition = true;
        setDrawingCacheEnabled(true);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        sharedConstructing(context);
        this.prViewDefaultPosition = true;
        setDrawingCacheEnabled(true);
    }

    void initDrawing() {
        this.splashBitmap = MainActivity.bitmapClear.copy(Config.ARGB_8888, true);
        this.drawingBitmap = Bitmap.createBitmap(MainActivity.bitmapBlur).copy(Config.ARGB_8888, true);
        setImageBitmap(this.drawingBitmap);
        this.canvas = new Canvas(this.drawingBitmap);
        this.circlePath = new Path();
        this.drawPath = new Path();
        this.brushPath = new Path();
        this.circlePaint = new Paint(DRAG);
        this.circlePaint.setColor(-65536);
        this.circlePaint.setStyle(Style.STROKE);
        this.circlePaint.setStrokeWidth(5.0f);
        this.drawPaint = new Paint(DRAG);
        this.drawPaint.setStyle(Style.STROKE);
        this.drawPaint.setStrokeWidth(this.radius);
        this.drawPaint.setStrokeCap(Cap.ROUND);
        this.drawPaint.setStrokeJoin(Join.ROUND);
        setLayerType(DRAG, null);
        this.tempPaint = new Paint();
        this.tempPaint.setStyle(Style.FILL);
        this.tempPaint.setColor(-1);
        this.previewPaint = new Paint();
        this.previewPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        this.tempPreviewBitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
        this.canvasPreview = new Canvas(this.tempPreviewBitmap);
        this.dstRect = new Rect(NONE, NONE, 100, 100);
        this.logPaintGray = new Paint(this.drawPaint);
        this.logPaintGray.setShader(new BitmapShader(MainActivity.bitmapBlur, TileMode.CLAMP, TileMode.CLAMP));
        this.bitmapShader = new BitmapShader(this.splashBitmap, TileMode.CLAMP, TileMode.CLAMP);
        this.drawPaint.setShader(this.bitmapShader);
        this.logPaintColor = new Paint(this.drawPaint);
        new SaveCanvasLog().execute(new String[NONE]);
    }

    void updatePaintBrush() {
        try {
            this.drawPaint.setStrokeWidth(this.radius * resRatio);
        } catch (Exception e) {
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePreviewPaint();
    }

    void changeShaderBitmap() {
        this.bitmapShader = new BitmapShader(this.splashBitmap, TileMode.CLAMP, TileMode.CLAMP);
        this.drawPaint.setShader(this.bitmapShader);
        updatePreviewPaint();
    }

    void updatePreviewPaint() {
        if (MainActivity.bitmapClear.getWidth() > MainActivity.bitmapClear.getHeight()) {
            resRatio = ((float) MainActivity.displayWidth) / ((float) MainActivity.bitmapClear.getWidth());
            resRatio *= this.saveScale;
        } else {
            resRatio = this.origHeight / ((float) MainActivity.bitmapClear.getHeight());
            resRatio *= this.saveScale;
        }
        this.drawPaint.setStrokeWidth(this.radius * resRatio);
        this.drawPaint.setMaskFilter(new BlurMaskFilter(30.0f * resRatio, Blur.NORMAL));
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.matrix = new Matrix();
        this.m = new float[9];
        setImageMatrix(this.matrix);
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TouchImageView.this.mScaleDetector.onTouchEvent(event);
                TouchImageView.this.pCount2 = event.getPointerCount();
                TouchImageView.this.curr = new PointF(event.getX(), event.getY() - (((float) MainActivity.offsetBar.getProgress()) * 3.0f));
                TouchImageView.this.x = (TouchImageView.this.curr.x - TouchImageView.this.m[TouchImageView.ZOOM]) / TouchImageView.this.m[TouchImageView.NONE];
                TouchImageView.this.y = (TouchImageView.this.curr.y - TouchImageView.this.m[5]) / TouchImageView.this.m[4];
                switch (event.getAction()) {
                    case TouchImageView.NONE /*0*/:
                        TouchImageView.this.drawPaint.setStrokeWidth(TouchImageView.this.radius * TouchImageView.resRatio);
                        TouchImageView.this.drawPaint.setMaskFilter(new BlurMaskFilter(30.0f * TouchImageView.resRatio, Blur.NORMAL));
                        TouchImageView.this.drawPaint.getShader().setLocalMatrix(TouchImageView.this.matrix);
                        TouchImageView.this.oldX = 0.0f;
                        TouchImageView.this.oldY = 0.0f;
                        TouchImageView.this.last.set(TouchImageView.this.curr);
                        TouchImageView.this.start.set(TouchImageView.this.last);
                        if (!(TouchImageView.this.mode == TouchImageView.DRAG || TouchImageView.this.mode == TouchImageView.ZOOMDRAG)) {
                            TouchImageView.this.draw = true;
                            MainActivity.prView.setVisibility(TouchImageView.NONE);
                        }
                        TouchImageView.this.circlePath.reset();
                        TouchImageView.this.circlePath.moveTo(TouchImageView.this.curr.x, TouchImageView.this.curr.y);
                        TouchImageView.this.circlePath.addCircle(TouchImageView.this.curr.x, TouchImageView.this.curr.y, (TouchImageView.this.radius * TouchImageView.resRatio) / 2.0f, Direction.CW);
                        TouchImageView.this.drawPath.moveTo(TouchImageView.this.x, TouchImageView.this.y);
                        TouchImageView.this.brushPath.moveTo(TouchImageView.this.curr.x, TouchImageView.this.curr.y);
                        TouchImageView.this.showBoxPreview();
                        break;
                    case TouchImageView.DRAG /*1*/:
                        if (TouchImageView.this.mode == TouchImageView.DRAG) {
                            TouchImageView.this.matrix.getValues(TouchImageView.this.m);
                        }
                        int yDiff = (int) Math.abs(TouchImageView.this.curr.y - TouchImageView.this.start.y);
                        if (((int) Math.abs(TouchImageView.this.curr.x - TouchImageView.this.start.x)) < TouchImageView.ZOOMDRAG && yDiff < TouchImageView.ZOOMDRAG) {
                            TouchImageView.this.performClick();
                        }
                        if (TouchImageView.this.draw) {
                            TouchImageView.this.drawPaint.setStrokeWidth(TouchImageView.this.radius);
                            TouchImageView.this.drawPaint.setMaskFilter(new BlurMaskFilter(30.0f, Blur.NORMAL));
                            TouchImageView.this.drawPaint.getShader().setLocalMatrix(new Matrix());
                            TouchImageView.this.canvas.drawPath(TouchImageView.this.drawPath, TouchImageView.this.drawPaint);
                            new SaveCanvasLog().execute(new String[TouchImageView.NONE]);
                        }
                        MainActivity.prView.setVisibility(4);
                        TouchImageView.this.circlePath.reset();
                        TouchImageView.this.drawPath.reset();
                        TouchImageView.this.brushPath.reset();
                        TouchImageView.this.draw = false;
                        break;
                    case TouchImageView.ZOOM /*2*/:
                        if (TouchImageView.this.mode != TouchImageView.DRAG && TouchImageView.this.mode != TouchImageView.ZOOMDRAG) {
                            if (TouchImageView.this.draw) {
                                TouchImageView.this.circlePath.reset();
                                TouchImageView.this.circlePath.moveTo(TouchImageView.this.curr.x, TouchImageView.this.curr.y);
                                TouchImageView.this.circlePath.addCircle(TouchImageView.this.curr.x, TouchImageView.this.curr.y, (TouchImageView.this.radius * TouchImageView.resRatio) / 2.0f, Direction.CW);
                                TouchImageView.this.drawPath.lineTo(TouchImageView.this.x, TouchImageView.this.y);
                                TouchImageView.this.brushPath.lineTo(TouchImageView.this.curr.x, TouchImageView.this.curr.y);
                                TouchImageView.this.showBoxPreview();
                                int prvSize = (int) (((double) MainActivity.prView.getWidth()) * 1.3d);
                                TranslateAnimation animation;
                                if (TouchImageView.this.curr.x > ((float) prvSize) || TouchImageView.this.curr.y > ((float) prvSize) || !TouchImageView.this.prViewDefaultPosition) {
                                    if (TouchImageView.this.curr.x <= ((float) prvSize) && TouchImageView.this.curr.y >= ((float) (TouchImageView.this.viewHeight - prvSize)) && !TouchImageView.this.prViewDefaultPosition) {
                                        TouchImageView.this.prViewDefaultPosition = true;
                                        animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-(TouchImageView.this.viewHeight - MainActivity.prView.getWidth())));
                                        animation.setDuration(500);
                                        animation.setFillAfter(false);
                                        animation.setAnimationListener(new MyAnimationListener());
                                        MainActivity.prView.startAnimation(animation);
                                        break;
                                    }
                                }
                                TouchImageView.this.prViewDefaultPosition = false;
                                animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (TouchImageView.this.viewHeight - MainActivity.prView.getWidth()));
                                animation.setDuration(500);
                                animation.setFillAfter(false);
                                animation.setAnimationListener(new MyAnimationListener());
                                MainActivity.prView.startAnimation(animation);
                                break;
                            }
                        }
                        if (TouchImageView.this.pCount1 == TouchImageView.DRAG && TouchImageView.this.pCount2 == TouchImageView.DRAG) {
                            TouchImageView.this.matrix.postTranslate(TouchImageView.this.curr.x - TouchImageView.this.last.x, TouchImageView.this.curr.y - TouchImageView.this.last.y);
                        }
                        TouchImageView.this.last.set(TouchImageView.this.curr.x, TouchImageView.this.curr.y);
                        break;
                        break;
                    case ShareConstants.MAXIMUM_PHOTO_COUNT /*6*/:
                        if (TouchImageView.this.mode == TouchImageView.ZOOM) {
                            TouchImageView.this.mode = TouchImageView.NONE;
                            break;
                        }
                        break;
                }
                TouchImageView.this.pCount1 = TouchImageView.this.pCount2;
                TouchImageView.this.setImageMatrix(TouchImageView.this.matrix);
                TouchImageView.this.invalidate();
                return true;
            }
        });
    }

    void updateRefMetrix() {
        this.matrix.getValues(this.m);
    }

    void showBoxPreview() {
        buildDrawingCache();
        Bitmap cacheBit = Bitmap.createBitmap(getDrawingCache());
        this.canvasPreview.drawRect(this.dstRect, this.tempPaint);
        this.canvasPreview.drawBitmap(cacheBit, new Rect(((int) this.curr.x) - 100, ((int) this.curr.y) - 100, ((int) this.curr.x) + 100, ((int) this.curr.y) + 100), this.dstRect, this.previewPaint);
        MainActivity.prView.setImageBitmap(this.tempPreviewBitmap);
        destroyDrawingCache();
    }

    public void onDraw(Canvas c) {
        float[] imageMatrix = new float[9];
        this.matrix.getValues(imageMatrix);
        int transX = (int) imageMatrix[ZOOM];
        int transY = (int) imageMatrix[5];
        super.onDraw(c);
        float maxClipHeight = (this.origHeight * this.saveScale) + ((float) transY);
        if (transY < 0) {
            c.clipRect((float) transX, 0.0f, ((float) transX) + (this.origWidth * this.saveScale), maxClipHeight > ((float) this.viewHeight) ? (float) this.viewHeight : maxClipHeight, Op.REPLACE);
        } else {
            c.clipRect((float) transX, (float) transY, ((float) transX) + (this.origWidth * this.saveScale), maxClipHeight > ((float) this.viewHeight) ? (float) this.viewHeight : maxClipHeight, Op.REPLACE);
        }
        if (this.draw) {
            c.drawPath(this.brushPath, this.drawPaint);
            c.drawPath(this.circlePath, this.circlePaint);
        }
    }

    void fixTrans() {
        this.matrix.getValues(this.m);
        float transX = this.m[ZOOM];
        float transY = this.m[5];
        float fixTransX = getFixTrans(transX, (float) this.viewWidth, this.origWidth * this.saveScale);
        float fixTransY = getFixTrans(transY, (float) this.viewHeight, this.origHeight * this.saveScale);
        if (!(fixTransX == 0.0f && fixTransY == 0.0f)) {
            this.matrix.postTranslate(fixTransX, fixTransY);
        }
        this.matrix.getValues(this.m);
        updatePreviewPaint();
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans;
        float maxTrans;
        if (contentSize <= viewSize) {
            minTrans = 0.0f;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0.0f;
        }
        if (trans < minTrans) {
            return (-trans) + minTrans;
        }
        if (trans > maxTrans) {
            return (-trans) + maxTrans;
        }
        return 0.0f;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!this.onMeasureCalled) {
            this.viewWidth = MeasureSpec.getSize(widthMeasureSpec);
            this.viewHeight = MeasureSpec.getSize(heightMeasureSpec);
            if ((this.oldMeasuredHeight != this.viewWidth || this.oldMeasuredHeight != this.viewHeight) && this.viewWidth != 0 && this.viewHeight != 0) {
                this.oldMeasuredHeight = this.viewHeight;
                this.oldMeasuredWidth = this.viewWidth;
                if (this.saveScale == 1.0f) {
                    fitScreen();
                }
                this.onMeasureCalled = true;
            }
        }
    }

    void fitScreen() {
        Drawable drawable = getDrawable();
        if (drawable != null && drawable.getIntrinsicWidth() != 0 && drawable.getIntrinsicHeight() != 0) {
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();
            float scale = Math.min(((float) this.viewWidth) / ((float) bmWidth), ((float) this.viewHeight) / ((float) bmHeight));
            this.matrix.setScale(scale, scale);
            float redundantYSpace = (((float) this.viewHeight) - (((float) bmHeight) * scale)) / 2.0f;
            float redundantXSpace = (((float) this.viewWidth) - (((float) bmWidth) * scale)) / 2.0f;
            this.matrix.postTranslate(redundantXSpace, redundantYSpace);
            this.origWidth = ((float) this.viewWidth) - (2.0f * redundantXSpace);
            this.origHeight = ((float) this.viewHeight) - (2.0f * redundantYSpace);
            setImageMatrix(this.matrix);
            this.matrix.getValues(this.m);
            fixTrans();
        }
    }
}
