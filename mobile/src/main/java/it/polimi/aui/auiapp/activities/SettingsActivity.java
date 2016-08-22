package it.polimi.aui.auiapp.activities;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Set;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.utils.Commons;
import it.polimi.aui.auiapp.utils.SettingsManager;

/**
 * Manages the application settings
 */
public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        try
        {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException e)
        {
            Log.v(getString(R.string.app_name), "Problems with getSupportActionBar() in SettingsActivity!");
        }
    }

    /**
     * PreferenceFragment automatically manages preferences
     */
    public static class PreferencesFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Get settings and fragment managers
            final SettingsManager settingsManager = SettingsManager.getInstance(getActivity());
            final FragmentManager fragmentManager = getFragmentManager();

            // Get button for hours picker
            final Preference hoursPickerButton = findPreference(getString(R.string.hour_pickers_key));

            // Add onClick on the button: open dialog
            hoursPickerButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(final Preference preference)
                {
                    // Create custom dialog
                    HoursPickerDialogFragment dialog = new HoursPickerDialogFragment();

                    // Add onDismiss listener
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {
                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                            // Update preference summary
                            hoursPickerButton.setSummary(Commons.hoursToString(getActivity(), settingsManager.getStartHour(), settingsManager.getEndHour()));
                        }
                    });

                    // Display custom dialog
                    dialog.show(fragmentManager, "hourspicker");

                    return true;
                }
            });

            // Set initial summary for hours picker button
            hoursPickerButton.setSummary(Commons.hoursToString(getActivity(), settingsManager.getStartHour(), settingsManager.getEndHour()));

            // Get date utility and days preference
            DateFormatSymbols dfs = new DateFormatSymbols();
            MultiSelectListPreference daysPreference = (MultiSelectListPreference) findPreference(getString(R.string.days_key));

            // Set entries and values for days preference
            daysPreference.setEntries(Arrays.copyOfRange(dfs.getWeekdays(), 1, 8));
            daysPreference.setEntryValues(Arrays.copyOfRange(dfs.getShortWeekdays(), 1, 8));

            // Set initial summary for days preference
            String[] selectedDays = settingsManager.getGameDays();
            if(selectedDays!=null) daysPreference.setSummary(Commons.weekdaysArrayToString(getActivity(), selectedDays));

            // Add change listener on days preference
            daysPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    // Update value (we are overriding the default method so we need to do this manually)
                    MultiSelectListPreference daysPreference = (MultiSelectListPreference) preference;
                    daysPreference.setValues((Set<String>) newValue);

                    // Update summary
                    daysPreference.setSummary(Commons.weekdaysArrayToString(getActivity(), Commons.orderDaysFromSet((Set<String>) newValue)));

                    // Show info toast
                    showSchedulingEditNotice(getActivity());

                    // Return false: do not update the preference value
                    return false;
                }
            });

            // Listener for frequency preference
            Preference frequencyPreference = findPreference(getString(R.string.frequency_key));
            frequencyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    // Show info toast
                    showSchedulingEditNotice(getActivity());

                    // Return true: update the preference value
                    return true;
                }
            });
        }
    }

    /**
     * Custom picker for hours: just a Dialog with two NumberPickers
     */
    public static class HoursPickerDialogFragment extends DialogFragment
    {
        private DialogInterface.OnDismissListener onDismissListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            // Build alter dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set custom view and get components
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.fragment_hours_picker_dialog, null);
            final NumberPicker fromNumberPicker = (NumberPicker) view.findViewById(R.id.from_number_picker);
            final NumberPicker toNumberPicker = (NumberPicker) view.findViewById(R.id.to_number_picker);
            builder.setView(view);

            // Setup components
            final SettingsManager settingsManager = SettingsManager.getInstance(getActivity());
            setupHourPickers(fromNumberPicker, settingsManager.getStartHour());
            setupHourPickers(toNumberPicker, settingsManager.getEndHour());

            // Add buttons to the dialog
            builder
                .setPositiveButton(getString(R.string.hours_picker_confirm), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // Do nothing here, we overwrite this method later to validate data
                    }
                })
                .setNegativeButton(getString(R.string.hours_picker_cancel), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // Simply close the dialog
                    }
                });

            // Create and show the dialog
            final AlertDialog dialog = builder.create();
            dialog.show();

            // Overwrite positive button behaviour to validate data
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Get selected hours
                    int fromValue = fromNumberPicker.getValue();
                    int toValue = toNumberPicker.getValue();

                    // If from < to...
                    if(fromValue < toValue)
                    {
                        // Write data
                        String[] values = SettingsManager.getAvailableHours();
                        settingsManager.updateHours(Integer.parseInt(values[fromValue]), Integer.parseInt(values[toValue]));

                        // Close dialog
                        dialog.dismiss();

                        // Show info toast
                        showSchedulingEditNotice(getActivity());
                    }

                    // Otherwise show error and don't close the dialog
                    else
                    {
                        CharSequence text = getString(R.string.hours_picker_validation_error);
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(getActivity(), text, duration);
                        toast.show();
                    }
                }
            });

            return dialog;
        }

        /**
         * Custom method to add a dismiss listener (not present in DialogFragment by default)
         */
        public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener)
        {
            this.onDismissListener = onDismissListener;
        }

        /**
         * When onDismiss is called in the fragment, we forward the event to the registered
         * dismiss listener (if any) set by the parent fragment/activity
         */
        @Override
        public void onDismiss(DialogInterface dialog)
        {
            super.onDismiss(dialog);

            if(onDismissListener!=null)
            {
                onDismissListener.onDismiss(dialog);
            }
        }

        /**
         * Helper method to set all settings for a date picker
         */
        private void setupHourPickers(NumberPicker picker, int selectedHour)
        {
            // Set values
            String[] values = SettingsManager.getAvailableHours();
            picker.setMinValue(0);
            picker.setMaxValue(values.length - 1);
            picker.setDisplayedValues(values);
            picker.setValue(Arrays.asList(values).indexOf(selectedHour+""));

            // Do not wrap if we reach beginning/end
            picker.setWrapSelectorWheel(false);

            // Do not allow manual edit of value (= just select available options)
            picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        }
    }

    /**
     * Helper method to show a toast informing the user that changes will take place from tomorrow
     */
    private static void showSchedulingEditNotice(Context context)
    {
        CharSequence text = context.getString(R.string.scheduling_changes_notice);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}


