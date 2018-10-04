package developer.mobile.com.blur;

import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Shader;

public class BrushSize {
    private Paint paintInner;
    private Paint paintOuter = new Paint();
    private Path path;
    Shader shad = new Shader();

    public BrushSize() {
        this.paintOuter.setColor(-16777216);
        this.paintOuter.setStrokeWidth(8.0f);
        this.paintOuter.setStyle(Style.STROKE);
        this.paintInner = new Paint();
        this.paintInner.setColor(-7829368);
        this.paintInner.setStrokeWidth(8.0f);
        this.paintInner.setStyle(Style.FILL);
        this.path = new Path();
    }

    public void setCircle(float x, float y, float radius, Direction dir) {
        this.path.reset();
        this.path.addCircle(x, y, radius, dir);
    }

    public Path getPath() {
        return this.path;
    }

    public Paint getPaint() {
        return this.paintOuter;
    }

    public Paint getInnerPaint() {
        return this.paintInner;
    }

    public void setPaintOpacity(int opacity) {
        this.paintInner.setAlpha(opacity);
    }
}
