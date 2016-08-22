package it.polimi.aui.auiapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.animations.GameProgressBarAnimation;
import it.polimi.aui.auiapp.data.GameManager;
import it.polimi.aui.auiapp.utils.SettingsManager;

/**
 * Shows the user progress recap, with a progress bar for each difficulty level (% of completed games) and a textual table (games solved/show/to go today)
 */
public class ProfileProgressFragment extends Fragment
{
    private SettingsManager settingsManager;

    private final int PROGRESS_BAR_DURATION_MS = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile_progress, container, false);

        // Get user info
        settingsManager = SettingsManager.getInstance(getActivity());

        // Build game progress bars
        buildProgressBars(view);

        // Setup today recap
        setupProgressRecap(view);

        return view;
    }

    /**
     * Creates a progress bar for each difficulty level representing how many games the user solved in that difficulty level
     */
    private void buildProgressBars(View view)
    {
        GameManager gameManager = GameManager.getInstance(getActivity());
        String[] difficultyValues = getResources().getStringArray(R.array.difficulty_values);
        String[] difficultyNames = getResources().getStringArray(R.array.difficulties_array);
        TextView progressBarTitle;
        ProgressBar progressBar;
        GameProgressBarAnimation progressBarAnimation;
        int percentage;
        for(int i=0; i<difficultyValues.length; i++)
        {
            // Get title and bar
            progressBarTitle = (TextView) view.findViewById(getResources().getIdentifier("difficulty" + difficultyValues[i] + "_bar_title", "id", getActivity().getPackageName()));
            progressBar = (ProgressBar) view.findViewById(getResources().getIdentifier("difficulty" + difficultyValues[i] + "_bar", "id", getActivity().getPackageName()));

            // Get completed percentage in this difficulty
            percentage = gameManager.getSolvedGamesPercentage(Integer.parseInt(difficultyValues[i]));

            // Set values and start animation
            progressBarTitle.setText(difficultyNames[i]);
            progressBarAnimation = new GameProgressBarAnimation(progressBar, 0, percentage);
            progressBarAnimation.setDuration(PROGRESS_BAR_DURATION_MS);
            progressBar.startAnimation(progressBarAnimation);
        }
    }

    /**
     * Creates a textual recap of the user progress (games shown/solved/to go today)
     */
    private void setupProgressRecap(View view)
    {
        int scheduledToday = settingsManager.getGamesScheduledToday();
        int solvedToday = settingsManager.getGamesSolvedToday();
        int shownToday = settingsManager.getGamesShownToday();
        int toGoToday = scheduledToday - shownToday;
        try
        {
            ((TextView) view.findViewById(R.id.today_shown)).setText("" + shownToday);
            ((TextView) view.findViewById(R.id.today_solved)).setText("" + solvedToday);
            ((TextView) view.findViewById(R.id.today_to_go)).setText("" + toGoToday);
        }
        catch(NullPointerException e)
        {
            Log.v(getString(R.string.app_name), "Something wrong with today's progress recap, null text views!");
        }
    }
}
