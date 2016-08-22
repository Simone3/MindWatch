package it.polimi.aui.auiapp.communication;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.data.GameManager;
import it.polimi.aui.auiapp.model.games.Game;
import it.polimi.aui.auiapp.utils.SettingsManager;
import it.polimi.aui.common.messages.GameResultMessage;

/**
 * Service that listens for data coming from the wearable application
 * It receives the game result after the user completes the game on the wearable and manages it
 */
public class ReceiveFromWear extends WearableListenerService
{
    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        // If it's a game result message...
        if(messageEvent.getPath().equalsIgnoreCase(getString(R.string.game_result_message_name)))
        {
            // Get game result from message
            Gson gson = new Gson();
            String serializedMessage = new String(messageEvent.getData());
            GameResultMessage gameResultMessage = gson.fromJson(serializedMessage, GameResultMessage.class);

            // Retrieve game linked with the message
            GameManager manager = GameManager.getInstance(this);
            Game game = manager.getGameById(gameResultMessage.GAME_ID);

            // Manage user points
            SettingsManager settingsManager = SettingsManager.getInstance(this);
            settingsManager.updateUserPointsAfterGame(gameResultMessage.CORRECT, game);

            // Set game as solved and increase today solved count, if needed
            if(gameResultMessage.CORRECT)
            {
                manager.setGameAsSolved(game.getId());
                settingsManager.increaseGamesSolvedToday();
            }
        }
        else
        {
            super.onMessageReceived(messageEvent);
        }
    }
}
