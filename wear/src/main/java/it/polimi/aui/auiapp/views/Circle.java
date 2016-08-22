package it.polimi.aui.auiapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import it.polimi.aui.auiapp.R;

/**
 * Implementation of shape used for round wearable devices: displays a circle arc to represent a timeout current state
 */
public class Circle extends Shape
{
    private static final int START_ANGLE_POINT = 0;

    private final Paint paint;
    private RectF rect;

    private int circleColor;
    private float strokeWidth;

    public Circle(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Get custom parameters
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Circle,
                0, 0);
        try
        {
            circleColor = a.getColor(R.styleable.Circle_circleColor, Color.RED);
            strokeWidth = a.getDimension(R.styleable.Circle_circleStrokeWidth, 1);
        }
        finally
        {
            a.recycle();
        }

        // Build drawing
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);

        // Halve stroke width (used as square dimension) to make the shape touch the view border
        strokeWidth = strokeWidth/2;

        // Circle color
        paint.setColor(circleColor);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec)
    {
        // Get view size and build a RectF according to them
        int measuredWidth = MeasureSpec.getSize(widthSpec);
        int measuredHeight = MeasureSpec.getSize(heightSpec);
        if(rect==null)
        {
            float rectSize = Math.min(measuredWidth, measuredHeight) - strokeWidth;
            rect = new RectF(strokeWidth, strokeWidth, rectSize, rectSize);
        }

        // Must call this when overriding onMeasure
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, getAngle(), false, paint);
    }
}
