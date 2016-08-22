package it.polimi.aui.auiapp.model.games;

import it.polimi.aui.auiapp.model.other.BrainTrainingType;

/**
 * Describes a game where the user has to complete a sequence of images/numbers/etc. with a missing piece
 */
public class GameCompleteSequence extends Game
{
    public GameCompleteSequence(int _id, String content, String solution, String[] wrongOptions, String locationType, int difficulty)
    {
        super(_id, content, solution, wrongOptions, locationType, difficulty);
    }

    @Override
    public boolean isContentAnImage()
    {
        return true;
    }

    @Override
    public boolean areOptionsImages()
    {
        return true;
    }

    @Override
    public BrainTrainingType getBrainTrainingType()
    {
        return BrainTrainingType.PROBLEM_SOLVING;
    }
}
