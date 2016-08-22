package it.polimi.aui.common.messages;

import java.io.Serializable;

/**
 * Serializable class that is sent from the wearable to the mobile to represent a game outcome
 */
public final class GameResultMessage implements Serializable
{
    public final boolean CORRECT;
    public final int GAME_ID;

    public GameResultMessage(boolean correct, int gameId)
    {
        this.CORRECT = correct;
        this.GAME_ID = gameId;
    }
}
