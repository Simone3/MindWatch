package it.polimi.aui.auiapp.animations;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import it.polimi.aui.auiapp.views.Shape;

/**
 * Animates a generic shape (see "views" package)
 */
public class ShapeAnimation extends Animation
{
    private Shape shape;

    private float oldAngle;
    private float newAngle;

    public ShapeAnimation(Shape shape)
    {
        this.oldAngle = shape.getAngle();
        this.newAngle = 360;
        this.shape = shape;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation)
    {
        float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

        shape.setAngle(angle);
        shape.requestLayout();
    }
}


