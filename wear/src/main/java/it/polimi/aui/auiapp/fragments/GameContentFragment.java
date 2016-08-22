package it.polimi.aui.auiapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.animations.ShapeAnimation;
import it.polimi.aui.auiapp.utils.StorageManager;
import it.polimi.aui.auiapp.views.Shape;

/**
 * Fragment used by GameActivity to display the game content
 */
public class GameContentFragment extends Fragment
{
    private static final String CONTENT = "game_content";
    private static final String IS_IMAGE = "is_image";
    private static final String TIMEOUT = "timeout";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_game_content, container, false);

        // Set game content (need to do it in the stub since we have two different layouts, round and rect)
        final WatchViewStub stub = (WatchViewStub) view.findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                // Start animation for timeout
                Shape shape = (Shape) stub.findViewById(R.id.timeout);
                ShapeAnimation animation = new ShapeAnimation(shape);
                animation.setDuration(getArguments().getLong(TIMEOUT));
                shape.startAnimation(animation);

                // Get content container and inflater
                LinearLayout contentContainer = (LinearLayout) stub.findViewById(R.id.content_container);
                LayoutInflater inflater = LayoutInflater.from(getActivity());

                // Set image or text, based on the given game
                if (getArguments().getBoolean(IS_IMAGE))
                {
                    // Inflate image layout
                    ImageView imageView = (ImageView) inflater.inflate(R.layout.game_content_image, contentContainer, false);
                    contentContainer.addView(imageView);

                    // Get image from storage, set it in the layout and then delete it
                    imageView.setImageBitmap(StorageManager.getImageFromInternalStorage(getActivity(), getString(R.string.content_image_filename)));
                    StorageManager.deleteFileFromInternalStorage(getActivity(), getString(R.string.content_image_filename));
                }
                else
                {
                    // Get content
                    String content = getArguments().getString(CONTENT);
                    if(content!=null)
                    {
                        int contentSize = content.length();

                        // Inflate text layout
                        LinearLayout textLinearLayout = (LinearLayout) inflater.inflate(R.layout.game_content_text, contentContainer, false);
                        TextView textView = (TextView) textLinearLayout.findViewById(R.id.game_content_text);
                        contentContainer.addView(textLinearLayout);

                        // Set text
                        textView.setText(getArguments().getString(CONTENT));

                        // Set text size
                        int textSize;
                        if (contentSize <= Integer.parseInt(getString(R.string.content_max_length_for_big_font)))
                            textSize = R.dimen.content_big_text_size;
                        else if (contentSize <= Integer.parseInt(getString(R.string.content_max_length_for_medium_font)))
                            textSize = R.dimen.content_medium_text_size;
                        else textSize = R.dimen.content_small_text_size;
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(textSize));
                    }
                }
            }
        });

        return view;
    }

    /**
     * Allows to set fragment parameters without overriding the constructor
     * @param content the game content
     * @param isImage true if the game content is an image, false if it's textual
     * @param timeout milliseconds for content timeout
     * @return a GameContentFragment instance
     */
    public static GameContentFragment newInstance(String content, boolean isImage, long timeout)
    {
        GameContentFragment gameContentFragment = new GameContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CONTENT, content);
        bundle.putBoolean(IS_IMAGE, isImage);
        bundle.putLong(TIMEOUT, timeout);
        gameContentFragment.setArguments(bundle);

        return gameContentFragment;
    }
}
