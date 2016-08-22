package it.polimi.aui.auiapp.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.activities.GameActivity;
import it.polimi.aui.auiapp.animations.ShapeAnimation;
import it.polimi.aui.auiapp.utils.StorageManager;
import it.polimi.aui.auiapp.views.Shape;

/**
 * Fragment used by GameActivity to display the game options
 */
public class GameOptionsFragment extends Fragment
{
    private static final String OPTIONS = "game_options";
    private static final String ARE_IMAGES = "are_images";
    private static final String TIMEOUT = "timeout";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_game_options, container, false);
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

                // Get options
                String[] textualOptions = getArguments().getStringArray(OPTIONS);

                // Get max text length if we are dealing with textual options
                int textSize = 0;
                if(!getArguments().getBoolean(ARE_IMAGES))
                {
                    int maxLength = 0;
                    for(int i=0; i<textualOptions.length; i++)
                    {
                        if(textualOptions[i].length()>maxLength) maxLength = textualOptions[i].length();
                    }
                    if(maxLength<=Integer.parseInt(getString(R.string.option_max_length_for_big_font))) textSize = R.dimen.option_big_text_size;
                    else if(maxLength<=Integer.parseInt(getString(R.string.option_max_length_for_medium_font))) textSize = R.dimen.option_medium_text_size;
                    else textSize = R.dimen.option_small_text_size;
                }

                // Build table looping all options
                final TableLayout table = (TableLayout) stub.findViewById(R.id.options_table);
                TableRow row = null;
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View cell;
                TextView textView;
                Bitmap image;
                String filename;
                ImageView imageView;
                for(int i=0; i<textualOptions.length; i++)
                {
                    // Build new row every 2 cells (= 2-columns table)
                    if(i%2==0) row = (TableRow) inflater.inflate(R.layout.game_options_row, table, false);

                    // If the options are images...
                    if(getArguments().getBoolean(ARE_IMAGES))
                    {
                        // Get image from storage
                        filename = getString(R.string.option_image_filename_prefix)+textualOptions[i];
                        image = StorageManager.getImageFromInternalStorage(getActivity(), filename);
                        StorageManager.deleteFileFromInternalStorage(getActivity(), filename);

                        // Set image
                        cell = inflater.inflate(R.layout.game_option_cell_image, row, false);
                        imageView = (ImageView) cell.findViewById(R.id.option_image);
                        imageView.setImageBitmap(image);
                    }

                    // If the options are textual...
                    else
                    {
                        // Set text
                        cell = inflater.inflate(R.layout.game_option_cell_text, row, false);
                        textView = (TextView) cell.findViewById(R.id.option_text);
                        textView.setText(textualOptions[i]);

                        // Set text size
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(textSize));
                    }

                    // Add click listeners (use tag to save the answer position in the array)
                    cell.setTag(i);
                    cell.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            int selectedAnswerIndex = (int) v.getTag();

                            ((GameActivity) getActivity()).managePlayerAnswer(selectedAnswerIndex);
                        }
                    });

                    // Add cell to current row
                    row.addView(cell);

                    // Add row to table if needed
                    if((i+1)%2==0 || i==textualOptions.length-1) table.addView(row);
                }
            }
        });

        return view;
    }

    /**
     * Allows to set fragment parameters without overriding the constructor
     * @param options the array of options
     * @param areImages true if the game options are images, false if they are textual
     * @param timeout milliseconds for options timeout
     * @return a GameOptionsFragment instance
     */
    public static GameOptionsFragment newInstance(String[] options, boolean areImages, long timeout)
    {
        GameOptionsFragment gameOptionsFragment = new GameOptionsFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(OPTIONS, options);
        bundle.putBoolean(ARE_IMAGES, areImages);
        bundle.putLong(TIMEOUT, timeout);
        gameOptionsFragment.setArguments(bundle);

        return gameOptionsFragment;
    }
}
