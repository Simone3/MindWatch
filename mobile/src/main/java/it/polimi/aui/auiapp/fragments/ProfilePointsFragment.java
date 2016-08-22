package it.polimi.aui.auiapp.fragments;

import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.model.other.BrainTrainingType;
import it.polimi.aui.auiapp.utils.SettingsManager;

/**
 * Shows the user points recap, with a radar chart for each brain training type and a textual table (total points, today balance,...)
 */
public class ProfilePointsFragment extends Fragment
{
    private SettingsManager settingsManager;
    private RadarChart chart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile_points, container, false);

        // Get user info
        settingsManager = SettingsManager.getInstance(getActivity());

        // Setup user points chart
        chart = (RadarChart) view.findViewById(R.id.points_chart);
        chart.setDescription("");
        setPointsChartLayout();
        setPointsChartData();

        // Setup today recap
        setupPointsRecap(view);

        return view;
    }

    /**
     * Sets up the data for the radar chart (user points recap)
     */
    private void setPointsChartData()
    {
        // Check if the user has some points...
        if(settingsManager.getTotalUserPoints()>0)
        {
            // Get training types
            BrainTrainingType[] types = BrainTrainingType.values();
            String[] trainingTypes = new String[types.length];
            for (int i = 0; i < types.length; i++) trainingTypes[i] = types[i].getName(getActivity());
            int verticesNumber = types.length;

            // Get points for each category (y values)
            ArrayList<Entry> yValues = new ArrayList<>();
            int currentPoints;
            for (int i = 0; i < verticesNumber; i++)
            {
                currentPoints = settingsManager.getUserPointsByBrainTrainingType(types[i]);
                yValues.add(new Entry(currentPoints, i));
            }

            // Setup training type names (x values)
            ArrayList<String> xValues = new ArrayList<>();
            for (int i = 0; i < verticesNumber; i++)
            {
                xValues.add(trainingTypes[i % trainingTypes.length]);
            }

            // Build chart sets
            RadarDataSet set = new RadarDataSet(yValues, "");
            set.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
            set.setDrawFilled(true);
            set.setLineWidth(3f);
            ArrayList<RadarDataSet> sets = new ArrayList<>();
            sets.add(set);

            // Setup chart data
            RadarData data = new RadarData(xValues, sets);
            data.setValueTextSize(10f);
            data.setDrawValues(false);
            chart.setData(data);
            chart.invalidate();
        }

        // If the user has no points...
        else
        {
            // Show message
            chart.setNoDataText(getString(R.string.empty_radar_chart));
            Paint p = chart.getPaint(Chart.PAINT_INFO);
            p.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_no_data_text_size));
            p.setColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            chart.clear();
        }
    }

    /**
     * Sets up the layout for the radar chart (user points recap)
     */
    private void setPointsChartLayout()
    {
        // Web line
        chart.setWebLineWidth(1.0f);
        chart.setWebLineWidthInner(0.3f);
        chart.setWebAlpha(100);

        // X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_labels_text_size));

        // Y axis
        YAxis yAxis = chart.getYAxis();
        yAxis.setLabelCount(BrainTrainingType.values().length, false);
        yAxis.setTextSize(getResources().getDimensionPixelSize(R.dimen.chart_numbers_text_size));
        yAxis.setStartAtZero(true);

        // Disable legend
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }

    /**
     * Creates a textual recap of the user points (total points, today's balance, user title)
        */
    private void setupPointsRecap(View view)
    {
        int pointsToday = settingsManager.getTodayPointsBalance();
        int totalPoints = settingsManager.getTotalUserPoints();
        String levelName = settingsManager.getUserLevelName();
        try
        {
            ((TextView) view.findViewById(R.id.total_points)).setText("" + totalPoints);
            ((TextView) view.findViewById(R.id.points_today_balance)).setText("" + pointsToday);
            ((TextView) view.findViewById(R.id.user_level_name)).setText(levelName);
        }
        catch(NullPointerException e)
        {
            Log.v(getString(R.string.app_name), "Something wrong with today's points recap, null text views!");
        }
    }
}
