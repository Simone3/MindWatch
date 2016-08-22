package it.polimi.aui.auiapp.communication;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.common.communication.SendMessageToBestNode;
import it.polimi.aui.common.messages.GameResultMessage;

/**
 * Sends data to the mobile application
 */
public class SendToMobile
{
    private static final int CONNECTION_TIMEOUT_S = 5;

    private GoogleApiClient apiClient;
    private Context context;

    public SendToMobile(Context context)
    {
        this.context = context;
        apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .build();
    }

    /**
     * Sends a game result to the mobile application
     * @param gameResult the game outcome
     */
    public void sendGameResult(GameResultMessage gameResult)
    {
        // Do this in an AsyncTask to avoid blocking the UI thread
        new SendToMobileAsyncTask().execute(gameResult);
    }

    /**
     * AsyncTask to handle connection and sending
     */
    private class SendToMobileAsyncTask extends AsyncTask<GameResultMessage, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(GameResultMessage... params)
        {
            // Get parameter
            GameResultMessage gameResult;
            if(params!=null && params.length==1) gameResult = params[0];
            else return 0;

            // Connect to phone (blocking, we are in a separate thread)
            ConnectionResult connectionResult = apiClient.blockingConnect(CONNECTION_TIMEOUT_S, TimeUnit.SECONDS);
            if(connectionResult.isSuccess())
            {
                // Send message
                new SendMessageToBestNode(apiClient, context.getString(R.string.game_result_message_name), gameResult).start();
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result){}
    }
}
