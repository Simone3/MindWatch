package it.polimi.aui.auiapp.model.games;

import it.polimi.aui.auiapp.model.other.BrainTrainingType;

/**
 * Describes a game where the user has to answer to fast general-knowledge questions
 */
public class GameFastQuestions extends Game
{
    public GameFastQuestions(int _id, String content, String solution, String[] wrongOptions, String locationType, int difficulty)
    {
        super(_id, content, solution, wrongOptions, locationType, difficulty);
    }

    @Override
    public boolean isContentAnImage()
    {
        return false;
    }

    @Override
    public boolean areOptionsImages()
    {
        return false;
    }

    @Override
    public BrainTrainingType getBrainTrainingType()
    {
        return BrainTrainingType.PROBLEM_SOLVING;
    }
}
