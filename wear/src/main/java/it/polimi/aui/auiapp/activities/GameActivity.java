package it.polimi.aui.auiapp.activities;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.communication.SendToMobile;
import it.polimi.aui.auiapp.fragments.GameContentFragment;
import it.polimi.aui.auiapp.fragments.GameOptionsFragment;
import it.polimi.aui.auiapp.fragments.GameOverFragment;
import it.polimi.aui.common.messages.GameMessage;
import it.polimi.aui.common.messages.GameResultMessage;

/**
 * Main and only activity of the wearable application, it displays a game using the GameContentFragment,
 * GameOptionsFragment and GameOverFragment fragments
 */
public class GameActivity extends Activity
{
    private GameMessage gameMessage;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get intent parameters
        gameMessage = (GameMessage) getIntent().getSerializableExtra(getString(R.string.game_intent_message));

        // Show content fragment
        this.showContentFragment();
    }

    /**
     * Displays the GameContentFragment and starts the content timeout
     */
    private void showContentFragment()
    {
        // Display fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment gameContentFragment = GameContentFragment.newInstance(gameMessage.CONTENT, gameMessage.CONTENT_IS_ASSET, gameMessage.CONTENT_TIMEOUT);
        transaction.replace(R.id.fragment_container, gameContentFragment);
        transaction.commit();

        // Add timeout: when over the game options will be shown
        timeoutHandler = new Handler();
        timeoutRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                showOptionsFragment();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, gameMessage.CONTENT_TIMEOUT);
    }

    /**
     * Displays the GameOptionsFragment and starts the options timeout
     */
    private void showOptionsFragment()
    {
        // Display fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment gameOptionsFragment = GameOptionsFragment.newInstance(gameMessage.ALL_OPTIONS, gameMessage.OPTIONS_ARE_ASSETS, gameMessage.OPTIONS_TIMEOUT);
        transaction.replace(R.id.fragment_container, gameOptionsFragment);
        transaction.commit();

        // Add timeout: when over the player will loose the game
        timeoutHandler = new Handler();
        timeoutRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                manageGameOver(false, GameOverFragment.TIMEOUT);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, gameMessage.OPTIONS_TIMEOUT);
    }

    /**
     * Manages the player answer, checking if it's correct and calling the game over manager method
     * @param selectedAnswerIndex the index of the selected answer in the options array
     */
    public void managePlayerAnswer(int selectedAnswerIndex)
    {
        // Stop timeout
        timeoutHandler.removeCallbacks(timeoutRunnable);

        // Check if answer is correct
        boolean correct = selectedAnswerIndex==gameMessage.SOLUTION_INDEX;

        // Call game over manager with the appropriate type
        int gameOverFragmentType = correct ? GameOverFragment.CORRECT : GameOverFragment.WRONG;
        manageGameOver(correct, gameOverFragmentType);
    }

    /**
     * Displays the GameOverFragment, removes the notification that started the game and calls the method to send the result to the phone
     * @param correct true if the user answered correctly, false if incorrectly or timeout expired
     * @param gameOverFragmentType ID of the "game over type", see static constants in GameOverFragment
     */
    private void manageGameOver(boolean correct, int gameOverFragmentType)
    {
        // Put "game over" fragment in place of the game
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment gameOverFragment = GameOverFragment.newInstance(gameOverFragmentType);
        transaction.replace(R.id.fragment_container, gameOverFragment);
        transaction.commit();

        // Remove notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(Integer.parseInt(getString(R.string.game_notification_id)));

        // Send result to phone
        this.sendMessageToPhone(correct, gameMessage.GAME_ID);
    }

    /**
     * Calls the SendToMobile class to send the game result to the phone
     * @param correct true if player gave the correct answer
     * @param gameId the ID of the game
     */
    private void sendMessageToPhone(boolean correct, int gameId)
    {
        // Build result message
        GameResultMessage gameResult = new GameResultMessage(correct, gameId);

        // Send data to phone
        (new SendToMobile(this)).sendGameResult(gameResult);
    }
}


