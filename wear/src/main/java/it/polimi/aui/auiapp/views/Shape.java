package it.polimi.aui.auiapp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Abstract class representing a shape used for timeout display
 */
public abstract class Shape extends View
{
    private float angle = 0;

    public Shape(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * Getter
     * @return currently displayed angle (= portion of shape)
     */
    public float getAngle()
    {
        return angle;
    }

    /**
     * Setter
     * @param angle new displayed angle (= portion of shape)
     */
    public void setAngle(float angle)
    {
        this.angle = angle;
    }
}
