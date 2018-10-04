package developer.mobile.com.blur;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

public class BrushView extends View {
    BrushSize brushSize;
    boolean isBrushSize = true;
    float opacity;
    float ratioRadius;

    public BrushView(Context context) {
        super(context);
        initMyView();
    }

    public BrushView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMyView();
    }

    public BrushView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMyView();
    }

    public void initMyView() {
        this.brushSize = new BrushSize();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w != 0 && h != 0) {
            float radius;
            float x = ((float) w) / 2.0f;
            float y = ((float) h) / 2.0f;
            if (w > h) {
                radius = (this.ratioRadius * TouchImageView.resRatio) / 2.0f;
            } else {
                radius = (this.ratioRadius * TouchImageView.resRatio) / 2.0f;
            }
            if (((int) radius) * 2 > 150) {
                LayoutParams params = (LayoutParams) getLayoutParams();
                params.height = ((int) (radius * 2.0f)) + 40;
                params.width = ((int) (radius * 2.0f)) + 40;
                params.alignWithParent = true;
                setLayoutParams(params);
            }
            this.brushSize.setCircle(x, y, radius, Direction.CCW);
            canvas.drawPath(this.brushSize.getPath(), this.brushSize.getPaint());
            if (!this.isBrushSize) {
                canvas.drawPath(this.brushSize.getPath(), this.brushSize.getInnerPaint());
            }
        }
    }

    public void setShapeRadiusRatio(float ratio) {
        this.ratioRadius = ratio;
    }

    public void setShapeOpacity(float op) {
        this.opacity = op;
    }
}
