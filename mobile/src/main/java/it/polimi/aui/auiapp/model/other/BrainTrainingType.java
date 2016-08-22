package it.polimi.aui.auiapp.model.other;

import android.content.Context;
import android.graphics.Color;

import it.polimi.aui.auiapp.R;

/**
 * Describes the game training categories associated to each game
 */
public enum BrainTrainingType
{
    MEMORY(R.string.brain_training_type_memory, Color.RED),
    CALCULATION(R.string.brain_training_type_calculation, Color.GREEN),
    QUICKNESS(R.string.brain_training_type_quickness, Color.BLUE),
    LANGUAGE(R.string.brain_training_type_language, Color.YELLOW),
    PROBLEM_SOLVING(R.string.brain_training_type_problem_solving, Color.CYAN);

    private int color;
    private int stringId;

    BrainTrainingType(int stringId, int color)
    {
        this.stringId = stringId;
        this.color = color;
    }

    /**
     * The value used to identify the type
     * @return lowercase name of the type
     */
    public String getValue()
    {
        return this.name().toLowerCase();
    }

    /**
     * The displayed name of the type (taken from strings.xml)
     * @param context activity context
     * @return displayed name of the type
     */
    public String getName(Context context)
    {
        return context.getString(this.stringId);
    }

    /**
     * The type color
     * @return ID of the color
     */
    public int getColor()
    {
        return this.color;
    }
}
