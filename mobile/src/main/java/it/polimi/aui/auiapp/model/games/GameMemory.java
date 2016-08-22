package it.polimi.aui.auiapp.model.games;

import it.polimi.aui.auiapp.model.other.BrainTrainingType;

/**
 * Describes a game where the user has to use his/her memory to answer a question related to a previously seen image
 */
public class GameMemory extends Game
{
    public GameMemory(int _id, String content, String solution, String[] wrongOptions, String locationType, int difficulty)
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
        return false;
    }

    @Override
    public BrainTrainingType getBrainTrainingType()
    {
        return BrainTrainingType.MEMORY;
    }
}
