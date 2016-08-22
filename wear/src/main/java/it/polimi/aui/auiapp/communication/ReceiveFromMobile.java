package it.polimi.aui.auiapp.communication;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.io.InputStream;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.activities.GameActivity;
import it.polimi.aui.auiapp.utils.StorageManager;
import it.polimi.aui.common.messages.GameMessage;

/**
 * Service that listens for data coming from the mobile application
 * It receives the game data (game description and images if necessary) and creates a notification on the wearable
 * that, once opened, will call the GameActivity. If content and/or options are images it calls the StorageManager
 * to temporarily store them on the wearable device
 */
public class ReceiveFromMobile extends WearableListenerService
{
    private GoogleApiClient apiClient;

    @Override
    public void onCreate()
    {
        super.onCreate();
        apiClient = new GoogleApiClient.Builder(this)
                        .addApi(Wearable.API)
                        .build();
        apiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents)
    {
        for(DataEvent event : dataEvents)
        {
            if(event.getType() == DataEvent.TYPE_CHANGED)
            {
                DataItem item = event.getDataItem();
                if(item.getUri().getPath().equals(getString(R.string.shared_game_data_request_name)))
                {
                    // Build intent for notification content
                    Intent intent = new Intent(this, GameActivity.class);

                    // Get data sent by the phone
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                    // Get message
                    String serializedMessage = dataMapItem.getDataMap().getString(getString(R.string.shared_game_message_name));
                    Gson gson = new Gson();
                    GameMessage gameMessage = gson.fromJson(serializedMessage, GameMessage.class);
                    intent.putExtra(getString(R.string.game_intent_message), gameMessage);

                    // Save to storage content and/or options images, if needed
                    if(gameMessage.CONTENT_IS_ASSET)
                    {
                        Bitmap image = loadBitmapFromAsset(dataMapItem.getDataMap().getAsset(getString(R.string.shared_content_image)));
                        StorageManager.saveImageToInternalStorage(this, getString(R.string.content_image_filename), image);
                    }
                    if(gameMessage.OPTIONS_ARE_ASSETS)
                    {
                        Bitmap image;
                        for(String option : gameMessage.ALL_OPTIONS)
                        {
                            image = loadBitmapFromAsset(dataMapItem.getDataMap().getAsset(getString(R.string.shared_option_image_prefix)+option));
                            StorageManager.saveImageToInternalStorage(this, getString(R.string.option_image_filename_prefix) + option, image);
                        }
                    }

                    // Remove shared data, no need for it anymore
                    Wearable.DataApi.deleteDataItems(apiClient, item.getUri()); // , DataApi.FILTER_PREFIX

                    // Create pending intent to link with the notification
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.putExtra(EXTRA_EVENT_ID, eventId);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    // Create notification builder
                    NotificationCompat.Builder notificationBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.mipmap.ic_wear_notification)
                                    .setColor(getColor(R.color.colorPrimary))
                                    .setContentTitle(getString(R.string.challenge_notification_title))
                                    .setContentText(getString(R.string.challenge_notification_content))
                                    .setContentIntent(pendingIntent);

                    // Get an instance of the NotificationManager service
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                    // Build the notification and issues it with notification manager
                    notificationManager.notify(Integer.parseInt(getString(R.string.game_notification_id)), notificationBuilder.build());
                }
            }
        }
        super.onDataChanged(dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        super.onMessageReceived(messageEvent);
    }

    /**
     * Helper to get a Bitmap from the raw asset received from the phone
     * @param asset the asset received from the phone
     * @return the Bitmap image contained in the asset
     */
    private Bitmap loadBitmapFromAsset(Asset asset)
    {
        // Convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(apiClient, asset).await().getInputStream();
        if (assetInputStream == null) return null;

        // Decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
