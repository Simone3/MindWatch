package it.polimi.aui.auiapp.communication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.data.GameManager;
import it.polimi.aui.auiapp.model.games.Game;
import it.polimi.aui.auiapp.utils.SettingsManager;
import it.polimi.aui.common.communication.SendMessageToBestNode;
import it.polimi.aui.common.messages.GameMessage;

/**
 * Sends data to the wearable application
 * Connection to the wearable device is done in a blocking way since we are running in a separate thread (games are sent by the AlarmReceiver at random times, with no user interaction)
 */
public class SendToWear
{
    private static final int CONNECTION_TIMEOUT_S = 5;

    private GoogleApiClient apiClient;
    private Context context;

    public SendToWear(Context context)
    {
        this.context = context;
        apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .build();
    }

    /**
     * Selects a random game from the database and sends it to the wearable application, sending images alongside the game content if necessary
     */
    public void sendRandomGame()
    {
        // Connect to wearable
        ConnectionResult connectionResult = apiClient.blockingConnect(CONNECTION_TIMEOUT_S, TimeUnit.SECONDS);
        if(connectionResult.isSuccess())
        {
            // Get user options
            SettingsManager settingsManager = SettingsManager.getInstance(context);
            int difficulty = settingsManager.getDifficulty();

            // Get random game with the given parameters
            GameManager manager = GameManager.getInstance(context);
            Game game = manager.getUnsolvedRandomGame(difficulty);
            if(game==null) return;
            String[] allOptions = game.getAllOptions();
            int solutionIndex = Arrays.asList(allOptions).indexOf(game.getSolution());
            GameMessage gameMessage = new GameMessage
            (
                game.getId(),
                game.getContent(),
                solutionIndex,
                allOptions,
                game.isContentAnImage(),
                game.areOptionsImages(),
                game.getContentScreenTimeout(),
                game.getOptionsScreenTimeout()
            );

            // Build request to send data to wear
            PutDataMapRequest dataMap = PutDataMapRequest.create(context.getString(R.string.shared_game_data_request_name));

            // Add serialized message
            Gson gson = new Gson();
            String serializedMessage = gson.toJson(gameMessage);
            dataMap.getDataMap().putString(context.getString(R.string.shared_game_message_name), serializedMessage);

            // Add images for content and/or options, if needed
            if(game.isContentAnImage())
            {
                Asset asset = createAssetFromGameImageName(game.getContent());
                dataMap.getDataMap().putAsset(context.getString(R.string.shared_content_image), asset);
            }
            if(game.areOptionsImages())
            {
                Asset asset;
                for(String option : game.getAllOptions())
                {
                    asset = createAssetFromGameImageName(option);
                    dataMap.getDataMap().putAsset(context.getString(R.string.shared_option_image_prefix)+option, asset);
                }
            }

            // Send data to wear
            PutDataRequest request = dataMap.asPutDataRequest();
            Wearable.DataApi.putDataItem(apiClient, request);

            // We need to send a "fake" message (content is "true") to the wearable in order to receive immediately the data (emulator bug?)
            new SendMessageToBestNode(apiClient, context.getString(R.string.fake_message_phone_to_wear_name), true).start();

            // Increase today's counter
            settingsManager.increaseGamesShownToday();
        }
    }

    /**
     * Gets an image name, retrieves it from the "drawable" folder and converts it to an Asset, needed
     * in order to send it to the wearable application
     */
    private Asset createAssetFromGameImageName(String gameImageName)
    {
        int resourceId = context.getResources().getIdentifier(gameImageName, "drawable", context.getPackageName());
        if(resourceId==0) return null;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }
}
