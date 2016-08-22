package it.polimi.aui.auiapp.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.communication.SendToWear;

/**
 * Receives all alarms set up by the application on the phone (game alarm and midnight alarm) and the Android's
 * BootCompleted alarm to restart them
 */
public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Get wakelock (need to keep device awake)
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getSimpleName());
        wakeLock.acquire();

        // If we have an action...
        if(intent.getAction()!=null)
        {
            // If the device just restarted...
            if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            {
                // Restart all alarms (midnight + daily)
                AlarmScheduler.getInstance().startAllAlarms(context);
            }

            // If it's the midnight alarm...
            else if(intent.getAction().equals(context.getString(R.string.midnight_action)))
            {
                // Setup the daily alarms
                AlarmScheduler.getInstance().setupTodayAlarms(context, true);
            }

            // If it's a game alarm...
            else if(intent.getAction().equals(context.getString(R.string.notification_action)))
            {
                // Send game to wear
                (new SendToWearAsyncTask(context)).execute();
            }
        }

        // Release wakelock
        wakeLock.release();
    }

    /**
     * AsyncTask to handle communication with SendToWear (cannot do it in the UI thread)
     */
    private class SendToWearAsyncTask extends AsyncTask<Integer, Integer, Integer>
    {
        private Context context;

        public SendToWearAsyncTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Integer... params)
        {
            (new SendToWear(context)).sendRandomGame();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result){}
    }
}
