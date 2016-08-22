package it.polimi.aui.auiapp.places;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages queries to the Google Places API
 */
public class PlacesManager
{
    private static final int CONNECTION_TIMEOUT_S = 5;
    private static final int QUERY_TIMEOUT_S = 5;
    private static final float MIN_LIKELIHOOD = 0.5f;

    private GoogleApiClient googleApiClient;
    private Context context;

    public PlacesManager(Context context)
    {
        this.context = context;
        googleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    /**
     * Returns the most likely GamePlace that the user is at right now
     * @return the user current position or GamePlace.NONE if the user is not in any recognized place
     */
    public GamePlace getCurrentPlace()
    {
        // Default place
        GamePlace place = GamePlace.NONE;

        // Check if we have location permission (for dynamic permissions, API >= 23)
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {
            // Try to connect to Google APIs
            ConnectionResult connectionResult = googleApiClient.blockingConnect(CONNECTION_TIMEOUT_S, TimeUnit.SECONDS);
            if(connectionResult.isSuccess())
            {
                // Try to get current place(s)
                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null);
                PlaceLikelihoodBuffer likelyPlaces = result.await(QUERY_TIMEOUT_S, TimeUnit.SECONDS);

                // If query was a success...
                if(likelyPlaces.getStatus().isSuccess())
                {
                    // Loop places
                    for(PlaceLikelihood placeLikelihood : likelyPlaces)
                    {
                        // If actually have something...
                        if(placeLikelihood!=null && placeLikelihood.getPlace()!=null)
                        {
                            // ...and it's a likely position...
                            if(placeLikelihood.getLikelihood() >= MIN_LIKELIHOOD)
                            {
                                // ...and we actually have a place...
                                List<Integer> places = placeLikelihood.getPlace().getPlaceTypes();
                                if(places!=null && places.size()>0)
                                {
                                    // Save it
                                    place = filterPlaceName(places);

                                    // Exit from loop if we got something
                                    if(!place.equals(GamePlace.NONE)) break;
                                }
                            }
                        }
                    }
                }

                // Release buffer and close connection
                likelyPlaces.release();
                googleApiClient.disconnect();
            }
        }

        // Return final place
        return place;
    }

    /**
     * Helper to translate a list of Google API Places (list of IDs) into a GamePlace
     * @param googlePlacesList the list of the Google API Places
     * @return a GamePlace
     */
    private GamePlace filterPlaceName(List<Integer> googlePlacesList)
    {
        // Loop all places
        for(Integer place: googlePlacesList)
        {
            // Check if we have a match
            switch(place)
            {
                case Place.TYPE_ART_GALLERY:
                case Place.TYPE_MUSEUM:
                    return GamePlace.ART;

                case Place.TYPE_BANK:
                    return GamePlace.BANK;

                case Place.TYPE_BAR:
                case Place.TYPE_CAFE:
                case Place.TYPE_FOOD:
                case Place.TYPE_MEAL_TAKEAWAY:
                case Place.TYPE_NIGHT_CLUB:
                case Place.TYPE_RESTAURANT:
                    return GamePlace.RESTAURANT;

                case Place.TYPE_BOOK_STORE:
                case Place.TYPE_LIBRARY:
                    return GamePlace.BOOKS;

                case Place.TYPE_CONVENIENCE_STORE:
                case Place.TYPE_DEPARTMENT_STORE:
                case Place.TYPE_GROCERY_OR_SUPERMARKET:
                case Place.TYPE_SHOPPING_MALL:
                case Place.TYPE_STORE:
                    return GamePlace.MALL;

                case Place.TYPE_DENTIST:
                case Place.TYPE_DOCTOR:
                case Place.TYPE_HOSPITAL:
                case Place.TYPE_PHARMACY:
                    return GamePlace.HOSPITAL;

                case Place.TYPE_GYM:
                    return GamePlace.GYM;

                case Place.TYPE_MOVIE_RENTAL:
                case Place.TYPE_MOVIE_THEATER:
                    return GamePlace.MOVIES;

                case Place.TYPE_POST_OFFICE:
                    return GamePlace.POST_OFFICE;

                case Place.TYPE_UNIVERSITY:
                case Place.TYPE_SCHOOL:
                    return GamePlace.SCHOOL;

                case Place.TYPE_STADIUM:
                    return GamePlace.STADIUM;

                case Place.TYPE_SUBWAY_STATION:
                case Place.TYPE_TRAIN_STATION:
                    return GamePlace.TRAINS;
            }
        }

        // No match, return generic type
        return GamePlace.NONE;
    }
}
