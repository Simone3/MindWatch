package it.polimi.aui.auiapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import java.util.ArrayList;

import it.polimi.aui.auiapp.R;

/**
 * Implementation of shape used for square wearable devices: displays a square "arc" to represent a timeout current state
 */
public class Square extends Shape
{
    private final Paint paint;

    private int squareColor;

    private float rectSize;
    private float strokeWidth;

    private ArrayList<Float> lines = new ArrayList<>();

    private float[] currentStartingPoint;
    private float[] currentEndingPoint;

    private float currentStartingAngle = 0;

    private int currentSegmentId = 0;

    public Square(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Get custom parameters
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Square,
                0, 0);
        try
        {
            squareColor = a.getColor(R.styleable.Square_squareColor, Color.RED);
            strokeWidth = a.getDimension(R.styleable.Square_squareStrokeWidth, 1);
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
        paint.setStrokeCap(Paint.Cap.SQUARE);

        // Halve stroke width (used as square dimension) to make the shape touch the view border
        strokeWidth = strokeWidth/2;

        // Square color
        paint.setColor(squareColor);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec)
    {
        // Get view size and build a RectF according to them
        int measuredWidth = MeasureSpec.getSize(widthSpec);
        int measuredHeight = MeasureSpec.getSize(heightSpec);
        rectSize = Math.min(measuredWidth, measuredHeight) - strokeWidth;

        // Must call this when overriding onMeasure
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // Get point corresponding to current angle
        currentEndingPoint = getPointByAngle(getAngle());

        // Set default starting point
        if(currentStartingPoint==null) currentStartingPoint = currentEndingPoint;

        // Draw every saved line
        if(lines.size()>=4) for(int i=0; i<lines.size(); i=i+4)
        {
            canvas.drawLine(lines.get(i), lines.get(i+1), lines.get(i+2), lines.get(i+3), paint);
        }

        // Draw current line
        canvas.drawLine(currentStartingPoint[0], currentStartingPoint[1], currentEndingPoint[0], currentEndingPoint[1], paint);
    }

    /**
     * Get the square point corresponding to the current angle
     * For example angle=45 corresponds to the south-east corner of the square,
     * angle=180 to the middle point of the western side of the square and so on.
     *
     * NOTE: Android coordinate system is not |_ but |¯ so y values are inverted!
     */
    private float[] getPointByAngle(float angle)
    {
        // Get the angle bounds of the current angle
        float[] bounds = getAngleBounds(angle);

        // Build constants
        float lengthPerGrade = (rectSize-strokeWidth)/(bounds[1]-bounds[0]);
        float[] result = new float[2];

        // Check in which square side is the current angle and set the point values
        int coordinateToSet;
        float sum;
        float sign;
        if(angle>=45 && angle<=135)
        {
            if(currentSegmentId!=1)
            {
                if(currentSegmentId!=0) completedCurrentSegment();
                currentSegmentId = 1;
            }

            coordinateToSet = 0;
            result[1] = rectSize;
            sum = strokeWidth;
            sign = 1;
        }
        else if(angle>=135 && angle<=225)
        {
            if(currentSegmentId!=2)
            {
                if(currentSegmentId!=0) completedCurrentSegment();
                currentSegmentId = 2;
            }

            coordinateToSet = 1;
            result[0] = strokeWidth;
            sum = strokeWidth;
            sign = 1;
        }
        else if(angle>=225 && angle<=315)
        {
            if(currentSegmentId!=3)
            {
                if(currentSegmentId!=0) completedCurrentSegment();
                currentSegmentId = 3;
            }

            coordinateToSet = 0;
            result[1] = strokeWidth;
            sum = rectSize;
            sign = -1;
        }
        else
        {
            if(currentSegmentId!=4)
            {
                if(currentSegmentId!=0) completedCurrentSegment();
                currentSegmentId = 4;
            }

            coordinateToSet = 1;
            result[0] = rectSize;
            sum = rectSize;
            sign = -1;
        }

        result[coordinateToSet] = sum+sign*lengthPerGrade*(bounds[1]-angle);

        return result;
    }

    /**
     * Return bounds (angles corresponding to the square corners) for current angle
     */
    private float[] getAngleBounds(float angle)
    {
        if(angle>=0 && angle<=45) return new float[]{-45, 45};
        if(angle>=45 && angle<=135) return new float[]{45, 135};
        if(angle>=135 && angle<=225) return new float[]{135, 225};
        if(angle>=225 && angle<=315) return new float[]{225, 315};
        return new float[]{315, 405};
    }

    /**
     * When we complete a side of the square (e.g. pass from angle 44 to 46) we need to save it in the list
     * (need to draw it at each step of the animation) and update the starting point
     */
    private void completedCurrentSegment()
    {
        // Get all corner points between the currentStartingAngle and the currentEndingAngle (the angle set by the animation)
        ArrayList<Float> cornerPoints = new ArrayList<>();
        float lastCornerAngle = 0;
        float[] tempPoint;
        for(float angle: new float[]{45,135,225,315})
        {
            if(angle>currentStartingAngle && angle<=getAngle())
            {
                tempPoint = getCornerPointByAngle(angle);
                cornerPoints.add(tempPoint[0]);
                cornerPoints.add(tempPoint[1]);
                lastCornerAngle = angle;
            }
        }

        // For each corner point, add a line in the list
        for(int j=0; j<cornerPoints.size(); j=j+2)
        {
            lines.add((j==0)?currentStartingPoint[0]:cornerPoints.get(j-2));
            lines.add((j==0)?currentStartingPoint[1]:cornerPoints.get(j-1));
            lines.add(cornerPoints.get(j));
            lines.add(cornerPoints.get(j+1));
        }

        // Update starting point/angle
        currentStartingPoint = new float[]{cornerPoints.get(cornerPoints.size()-2), cornerPoints.get(cornerPoints.size()-1)};
        currentStartingAngle = lastCornerAngle;
    }

    /**
     * Returns the corner point of the square linked to the given corner angle
     */
    private float[] getCornerPointByAngle(float cornerAngle)
    {
        if(cornerAngle==45) return new float[]{rectSize, rectSize};
        if(cornerAngle==135) return new float[]{strokeWidth, rectSize};
        if(cornerAngle==225) return new float[]{strokeWidth, strokeWidth};
        return new float[]{rectSize, strokeWidth};
    }
}
