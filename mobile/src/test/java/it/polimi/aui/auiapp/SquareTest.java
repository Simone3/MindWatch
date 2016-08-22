package it.polimi.aui.auiapp;

import org.junit.Test;

import java.util.ArrayList;

public class SquareTest
{
    private static final int START_ANGLE_POINT = 0;

    private int squareColor;

    private float rectSize = 27;
    private float strokeWidth = 5;

    //private float previousAngle = 0;

    //private float[] previousPoint = new float[2];

    private float[] lines2;










    private ArrayList<Float> lines = new ArrayList<>();

    private float[] currentStartingPoint;
    private float[] currentEndingPoint;

    private float currentStartingAngle = START_ANGLE_POINT;
    private float currentEndingAngle = START_ANGLE_POINT;

    private int currentSegmentId = 0;





    @Test
    public void testSquare()
    {
        currentStartingAngle = 100;
        currentEndingAngle = 140;
        System.out.println();
        System.out.println("------" + currentStartingAngle + "-" + currentEndingAngle+"---------");
        currentEndingPoint = getPointByAngle(currentEndingAngle);
        if(currentStartingPoint==null) currentStartingPoint = currentEndingPoint;
        completedCurrentSegment();
    }

    private float[] getPointByAngle(float angle)
    {
        System.out.println("ANGLE "+angle+": "+currentStartingAngle+"-"+currentEndingAngle+"---"+currentSegmentId);

        float[] bounds = getAngleBounds(angle);

        float lengthPerGrade = (rectSize-strokeWidth)/(bounds[1]-bounds[0]);

        float[] result = new float[2];

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
            result[1] = strokeWidth;
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
            sum = rectSize;
            sign = -1;
        }
        else if(angle>=225 && angle<=315)
        {
            if(currentSegmentId!=3)
            {
                if(currentSegmentId!=0) completedCurrentSegment();
                currentSegmentId = 3;
            }

            coordinateToSet = 0;
            result[1] = rectSize;
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
            sum = strokeWidth;
            sign = 1;
        }

        result[coordinateToSet] = sum+sign*lengthPerGrade*(bounds[1]-angle);

        return result;
    }

    private float[] getAngleBounds(float angle)
    {
        if(angle>=0 && angle<=45) return new float[]{-45, 45};
        if(angle>=45 && angle<=135) return new float[]{45, 135};
        if(angle>=135 && angle<=225) return new float[]{135, 225};
        if(angle>=225 && angle<=315) return new float[]{225, 315};
        return new float[]{315, 405};
    }

    private void completedCurrentSegment()
    {
        float[] cornerPoint = null;
        for(float cornerAngle: new float[]{45,135,225,315})
        {
            if(cornerAngle>=currentStartingAngle && cornerAngle<=currentEndingAngle)
            {
                cornerPoint = getCornerPointByAngle(cornerAngle);
                break;
            }
        }

        System.out.println("Adding to LINES: " + currentStartingPoint[0] + "-" + currentStartingPoint[1] + "---" + cornerPoint[0] + "-" + cornerPoint[1]);

        lines.add(currentStartingPoint[0]);
        lines.add(currentStartingPoint[1]);
        lines.add(cornerPoint[0]);
        lines.add(cornerPoint[1]);

        currentStartingPoint = cornerPoint;
    }


    private float[] getCornerPointByAngle(float cornerAngle)
    {
        if(cornerAngle==45) return new float[]{rectSize, strokeWidth};
        if(cornerAngle==135) return new float[]{strokeWidth, strokeWidth};
        if(cornerAngle==225) return new float[]{strokeWidth, rectSize};
        return new float[]{rectSize, rectSize};
    }


/*
    private float rectSize = 27;
    private float strokeWidth = 5;

    private float previousAngle = 50;
    private float angle = 75;


    @Test
    public void testSquare()
    {
        previousAngle = 50;
        angle = 75;
        printTest();

        previousAngle = 45;
        angle = 135;
        printTest();

        previousAngle = 140;
        angle = 141;
        printTest();

        previousAngle = 180;
        angle = 224;
        printTest();

        previousAngle = 230;
        angle = 280;
        printTest();

        previousAngle = 320;
        angle = 350;
        printTest();

        previousAngle = 350;
        angle = 10;
        printTest();

        previousAngle = 10;
        angle = 30;
        printTest();

        previousAngle = 100;
        angle = 150;
        printTest();
    }

    private void printTest()
    {
        System.out.println();
        System.out.println("------"+previousAngle+"-"+angle+"---------");

        float[] point1 = getPointByAngle(previousAngle);
        float[] point2 = getPointByAngle(angle);

        System.out.println("Point 1 is "+point1[0]+":"+point1[1]);
        System.out.println("Point 2 is "+point2[0]+":"+point2[1]);
    }

    private float[] getPointByAngle(float angle)
    {
        float[] bounds = getAngleBounds(angle);

        float lengthPerGrade = (rectSize-strokeWidth)/(bounds[1]-bounds[0]);

        float[] result = new float[2];

        int coordinateToSet;
        float sum;
        float sign;
        if(angle>=45 && angle<=135)
        {
            coordinateToSet = 0;
            result[1] = strokeWidth;
            sum = strokeWidth;
            sign = 1;
        }
        else if(angle>=135 && angle<=225)
        {
            coordinateToSet = 1;
            result[0] = strokeWidth;
            sum = rectSize;
            sign = -1;
        }
        else if(angle>=225 && angle<=315)
        {
            coordinateToSet = 0;
            result[1] = rectSize;
            sum = rectSize;
            sign = -1;
        }
        else
        {
            coordinateToSet = 1;
            result[0] = rectSize;
            sum = strokeWidth;
            sign = 1;
        }

        result[coordinateToSet] = sum+sign*lengthPerGrade*(bounds[1]-angle);

        return result;
    }

    private float[] getAngleBounds(float angle)
    {
        if(angle>=0 && angle <= 45) return new float[]{-45, 45};
        if(angle>=45 && angle<=135) return new float[]{45, 135};
        if(angle>=135 && angle<=225) return new float[]{135, 225};
        if(angle>=225 && angle<=315) return new float[]{225, 315};
        return new float[]{315, 405};
    }*/
}
