package osm.mapnotes;

public class Util {

    static String formatLatitude(double lat) {

        String text;

        if (lat > 0.0) {

            text = "N ";
        } else {
            text = "S ";

            lat = Math.abs(lat);
        }

        text+=formatDegress(lat);

        return text;
    }

    static String formatLongitude(double lon) {

        String text;

        if (lon > 0.0) {

            text = "E ";
        } else {
            text = "W ";

            lon = Math.abs(lon);
        }

        text+=formatDegress(lon);

        return text;
    }

    static String formatDegress(double value) {

        double degress=Math.floor(value);

        value-=degress;

        value*=60.0;

        double minutes=Math.floor(value);

        value-=minutes;

        value*=60.0;

        return String.format("%d\u00B0 %d' %.02f\"", (int)degress, (int)minutes, value);
    }
}
