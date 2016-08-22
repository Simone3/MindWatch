package it.polimi.aui.auiapp.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.polimi.aui.auiapp.R;

/**
 * Fragment used by GameActivity to display the game outcome
 */
public class GameOverFragment extends Fragment
{
    private static final String TYPE = "type";

    public static final int CORRECT = 1;
    public static final int WRONG = 2;
    public static final int TIMEOUT = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_game_over, container, false);

        // Get views
        TextView textView = (TextView) view.findViewById(R.id.result_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.result_image);

        // Set text and image
        int type = getArguments().getInt(TYPE);
        switch(type)
        {
            case CORRECT:
                textView.setText(R.string.game_result_right);
                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.game_result_right));
                imageView.setImageResource(R.drawable.game_result_right);
                break;

            case WRONG:
                textView.setText(R.string.game_result_wrong);
                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.game_result_wrong));
                imageView.setImageResource(R.drawable.game_result_wrong);
                break;

            case TIMEOUT:
                textView.setText(R.string.game_result_timeout);
                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.game_result_timeout));
                imageView.setImageResource(R.drawable.game_result_timeout);
                break;
        }

        return view;
    }

    /**
     * Allows to set fragment parameters without overriding the constructor
     * @param type the type of outcome, see constants CORRECT, WRONG and TIMEOUT
     * @return a GameOverFragment instance
     */
    public static GameOverFragment newInstance(int type)
    {
        GameOverFragment gameOverFragment = new GameOverFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);

        gameOverFragment.setArguments(bundle);

        return gameOverFragment;
    }
}
