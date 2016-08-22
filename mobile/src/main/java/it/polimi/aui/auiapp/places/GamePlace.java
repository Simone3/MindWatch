package it.polimi.aui.auiapp.places;

/**
 * Describes a place used for game localization
 */
public enum GamePlace
{
    NONE, RESTAURANT, MALL, SCHOOL, ART, BANK, BOOKS, HOSPITAL, GYM, MOVIES, STADIUM, POST_OFFICE, TRAINS;

    /**
     * The value used to identify the place
     * @return lowercase name of the place
     */
    public String getValue()
    {
        return this.name().toLowerCase();
    }
}
