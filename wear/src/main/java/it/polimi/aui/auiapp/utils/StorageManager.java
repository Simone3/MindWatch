package it.polimi.aui.auiapp.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Manages creation and deletion of files on the wearable device storage
 */
public class StorageManager
{
    /**
     * Saves a Bitmap to internal storage
     * @param context activity context
     * @param filename the name for the file to be stored
     * @param image the image to store
     */
    public static void saveImageToInternalStorage(Context context, String filename, Bitmap image)
    {
        try
        {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);

            image.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Gets a Bitmap from internal storage
     * @param context activity context
     * @param filename the name of the stored file
     * @return the saved Bitmap
     */
    public static Bitmap getImageFromInternalStorage(Context context, String filename)
    {
        try
        {
            InputStream is = context.openFileInput(filename);
            return BitmapFactory.decodeStream(is);
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes a file from internal storage
     * @param context activity context
     * @param filename the name of the stored file
     */
    public static void deleteFileFromInternalStorage(Context context, String filename)
    {
        context.deleteFile(filename);
    }
}
