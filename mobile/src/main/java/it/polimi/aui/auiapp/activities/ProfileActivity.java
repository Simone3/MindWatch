package it.polimi.aui.auiapp.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.alarms.AlarmScheduler;
import it.polimi.aui.auiapp.communication.SendToWear;
import it.polimi.aui.auiapp.fragments.ProfilePointsFragment;
import it.polimi.aui.auiapp.fragments.ProfileProgressFragment;
import it.polimi.aui.auiapp.utils.SettingsManager;

/**
 * Main activity on the phone, allows to
 * - see the user points via ProfilePointsFragment (first shown, accessible via drawer sidebar)
 * - see the user progress via ProfileProgressFragment (accessible via drawer sidebar)
 * - share progress (via button in action bar)
 * - go to settings (via button in action bar)
 * Also handles first-run actions, like asking permissions
 */
public class ProfileActivity extends AppCompatActivity
{
    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private View permissionRequestLayout;
    private ShareActionProvider shareActionProvider;

    private DrawerItem[] drawerItems;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence title;

    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        permissionRequestLayout = findViewById(R.id.profile_activity_container);

        // Get user info
        settingsManager = SettingsManager.getInstance(this);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get drawer (sidebar) options
        title = getTitle();
        String[] drawerOptionNames = getResources().getStringArray(R.array.drawer_options_titles);
        TypedArray drawerOptionIcons = getResources().obtainTypedArray(R.array.drawer_options_icons);
        drawerItems = new DrawerItem[Math.min(drawerOptionNames.length, drawerOptionIcons.length())];
        for(int i=0; i<drawerItems.length; i++)
        {
            drawerItems[i] = new DrawerItem(drawerOptionNames[i], drawerOptionIcons.getResourceId(i, -1));
        }
        drawerOptionIcons.recycle();

        // Get drawer pattern variables
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.left_drawer);

        // Set a custom shadow that overlays the main content when the drawer opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Set up the drawer's list view with items and click listener
        DrawerArrayAdapter adapter = new DrawerArrayAdapter(this, R.layout.drawer_list_item, drawerItems);
        drawerListView.setAdapter(adapter);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());

        // Enable ActionBar app icon to behave as action to toggle nav drawer
        try
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        catch (NullPointerException e)
        {
            Log.v(getString(R.string.app_name), "Problems with getSupportActionBar() in ProfileActivity!");
        }

        // ActionBarDrawerToggle ties together the the proper interactions between the sliding drawer and the action bar app icon
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        {
            @Override
            public void onDrawerClosed(View view)
            {
                getSupportActionBar().setTitle(title);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        // Default drawer item
        if (savedInstanceState == null)
        {
            selectDrawerItem(0);
        }

        // If it's the first time the application is run...
        if(settingsManager.isFirstRun())
        {
            // Start all alarms (midnight + games)
            AlarmScheduler.getInstance().startAllAlarms(this);

            // Show welcome message
            showWelcomeMessage();

            // Change first run flag
            settingsManager.setFirstRun(false);
        }

        // Otherwise only manage location permission (the first time it's managed after the welcome message)
        else
        {
            requestLocationPermission();
        }
    }

    /**
     * Dynamic permission request (location) for API >= 23
     */
    private void requestLocationPermission()
    {
        // If we don't already have the permission...
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            // If the user previously refused to grant the permission (and didn't set "Don't ask again")...
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show a message as a reminder
                Snackbar.make(permissionRequestLayout, getString(R.string.permission_request_explanation, getString(R.string.app_name)), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.permission_request_agree), new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                // If the user clicks on Agree, ask permission again
                                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                            }
                        }).show();
            }

            // If it's the first time we get here...
            else
            {
                // Ask permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate menu resource file
        getMenuInflater().inflate(R.menu.toolbar, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Set sharing intent
        Intent textShareIntent = new Intent(Intent.ACTION_SEND);
        textShareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, settingsManager.getTotalUserPoints(), getString(R.string.app_name)));
        textShareIntent.setType("text/plain");
        setShareIntent(textShareIntent);

        // Return true to display menu
        return true;
    }

    /**
     * Called to update the share intent
     */
    private void setShareIntent(Intent shareIntent)
    {
        if(shareActionProvider!=null)
        {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            // Go to settings
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Private class for handling clicks in the sidebar drawer
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectDrawerItem(position);
        }
    }

    /**
     * Handles the selected sidebar drawer option
     * @param position the position selected by the user
     */
    private void selectDrawerItem(int position)
    {
        // Update the main content by replacing fragments
        Fragment fragment;
        switch(position)
        {
            case 0:
                fragment = new ProfilePointsFragment();
                break;

            case 1:
                fragment = new ProfileProgressFragment();
                break;

            default:
                return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.profile_activity_container, fragment).commit();

        // Update selected item and title, then close the drawer
        drawerListView.setItemChecked(position, true);
        setTitle(drawerItems[position].title);
        drawerLayout.closeDrawer(drawerListView);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        this.title = title;
        try
        {
            getSupportActionBar().setTitle(this.title);
        }
        catch (NullPointerException e)
        {
            Log.v(getString(R.string.app_name), "Problems with getSupportActionBar() in ProfileActivity!");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Item for the drawer array adapter: title + icon resource ID
     */
    private class DrawerItem
    {
        public String title;
        public int iconId;

        public DrawerItem(String title, int iconId)
        {
            this.title = title;
            this.iconId = iconId;
        }
    }

    /**
     * ArrayAdapter for drawer items (parameters are DrawerItem, i.e. a text and an image)
     */
    private class DrawerArrayAdapter extends ArrayAdapter<DrawerItem>
    {
        Context context;
        int layoutResourceId;
        DrawerItem data[] = null;

        public DrawerArrayAdapter(Context context, int layoutResourceId, DrawerItem[] data)
        {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // Get the data item for this position
            DrawerItem drawerItem = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if(convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_list_item, parent, false);
            }

            // Lookup view for data population
            TextView textViewTitle = (TextView) convertView.findViewById(R.id.drawer_text);
            ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.drawer_icon);

            // Populate the data into the template view using the data object
            textViewTitle.setText(drawerItem.title);
            imageViewIcon.setImageResource(drawerItem.iconId);

            // Return the completed view to render on screen
            return convertView;
        }
    }

    /**
     * Shows a dialog to greet the user at first launch
     */
    private void showWelcomeMessage()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
        alertDialogBuilder.setTitle(R.string.welcome_message_title);

        alertDialogBuilder
                .setMessage(getString(R.string.welcome_message_content, getString(R.string.app_name)))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                        requestLocationPermission();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
