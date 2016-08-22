package it.polimi.aui.common.messages;

import java.io.Serializable;

/**
 * Serializable class that is sent from the mobile to the wearable to represent a game
 */
public final class GameMessage implements Serializable
{
    public final int GAME_ID;
    public final String CONTENT;
    public final String[] ALL_OPTIONS;
    public final int SOLUTION_INDEX;
    public final boolean CONTENT_IS_ASSET;
    public final boolean OPTIONS_ARE_ASSETS;
    public final long CONTENT_TIMEOUT;
    public final long OPTIONS_TIMEOUT;

    public GameMessage(int gameId, String content, int solutionIndex, String[] allOptions, boolean contentIsAsset, boolean optionsAreAssets, long contentTimeout, long optionsTimeout)
    {
        this.GAME_ID = gameId;
        this.CONTENT = content;
        this.ALL_OPTIONS = allOptions;
        this.SOLUTION_INDEX = solutionIndex;
        this.CONTENT_IS_ASSET = contentIsAsset;
        this.OPTIONS_ARE_ASSETS = optionsAreAssets;
        this.CONTENT_TIMEOUT = contentTimeout;
        this.OPTIONS_TIMEOUT = optionsTimeout;
    }
}
