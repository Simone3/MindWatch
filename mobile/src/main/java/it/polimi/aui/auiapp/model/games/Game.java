package it.polimi.aui.auiapp.model.games;

import java.util.Arrays;
import java.util.Collections;

import it.polimi.aui.auiapp.model.other.BrainTrainingType;

/**
 * Abstract class that represents a game
 */
public abstract class Game
{
    private int _id;
    private String content;
    private String solution;
    private String[] wrongOptions;
    private String locationType;
    private int difficulty;

    public Game(int _id, String content, String solution, String[] wrongOptions, String locationType, int difficulty)
    {
        this._id = _id;
        this.content = content;
        this.solution = solution;
        this.wrongOptions = wrongOptions;
        this.locationType = locationType;
        this.difficulty = difficulty;
    }

    /**
     * Getter
     * @return the game ID
     */
    public int getId()
    {
        return _id;
    }

    /**
     * Getter
     * @return the game content: may be a text (e.g. "1+1=?") or an image name (see isContentAnImage method)
     */
    public String getContent()
    {
        return content;
    }

    /**
     * Getter
     * @return the game solution: may be a text (e.g. "2") or an image name (see areOptionsImages method)
     */
    public String getSolution()
    {
        return solution;
    }

    /**
     * Getter
     * @return all game options (solution + wrong options): each of them may be a text (e.g. "2") or an image name (see areOptionsImages method)
     */
    public String[] getAllOptions()
    {
        String[] options = new String[wrongOptions.length+1];
        options[0] = this.getSolution();
        System.arraycopy(wrongOptions, 0, options, 1, wrongOptions.length);
        Collections.shuffle(Arrays.asList(options));
        return options;
    }

    /**
     * Getter
     * @return the game difficulty level
     */
    public int getDifficulty()
    {
        return difficulty;
    }


    /**
     * Returns the number of points that are to be added to the user points after this game is over, depending on the outcome
     * @param correct true if the user solved the game correctly
     * @return typically a number > 0 if correct, a number < 0 otherwise
     */
    public int getPointsChangeAfterAnswer(boolean correct)
    {
        return correct ? this.getPointsGainIfSuccess() : -this.getPointsLossIfFailure();
    }

    /**
     * Timeout in milliseconds for the content screen on the wearable
     * @return milliseconds for content timeout
     */
    public long getContentScreenTimeout()
    {
        if(difficulty==1) return 2500L;
        else if(difficulty==2) return 3500L;
        else return 4000L;
    }

    /**
     * Timeout in milliseconds for the options screen on the wearable
     * @return milliseconds for options timeout
     */
    public long getOptionsScreenTimeout()
    {
        if(difficulty==1) return 2500L;
        else if(difficulty==2) return 3500L;
        else return 4000L;
    }

    /**
     * Number of points to add if the user solved the game successfully
     * @return number > 0
     */
    public int getPointsGainIfSuccess()
    {
        return 10*this.getDifficulty();
    }

    /**
     * Number of points to subtract if the user solved the game successfully
     * @return number > 0
     */
    public int getPointsLossIfFailure()
    {
        return 3*this.getDifficulty();
    }

    /**
     * Tells if the game content is an image name
     * @return true if content is an image, false if content is textual
     */
    public abstract boolean isContentAnImage();

    /**
     * Tells if the game options are image names
     * @return true if options are images, false if options are textual
     */
    public abstract boolean areOptionsImages();

    /**
     * Getter
     * @return the brain training category linked with the game
     */
    public abstract BrainTrainingType getBrainTrainingType();
}
