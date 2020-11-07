package osm.mapnotes;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class KeepRightErrorMarker extends Marker {

    private String mErrorName = null;
    private String mMsgId = null;

    public KeepRightErrorMarker(MapView mapView, KeepRightErrorData data) {
        super(mapView);

        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        setDefaultIcon();
        setDraggable(false);

        if (data != null) {

            setPosition(data.mPosition);
            setId(data.mErrorId);
            setErrorName(data.mErrorName);
            setMsgId(data.mMsgId);
        }
    }

    public void setErrorName(String errorName) {

        mErrorName = errorName;
    }

    public void setMsgId(String msgId) {

        mMsgId = msgId;

        setTitle(mErrorName+" ["+mMsgId+"]");
    }

    public void selectIcon(Resources res) {

        int drawableId = R.drawable.error_circle;
        int color = Color.parseColor("black");

        String[][] values = {
                {"almost-junctions",                    "circle", "red"},
                {"deprecated tags",                     "circle", "green"},
                {"point of interest without name",      "circle", "blue"},
                {"fixme-tagged items",                  "circle", "cyan"},
                {"floating islands",                    "circle", "yellow"},
                {"highway-waterway",                    "circle", "teal"},
                {"highway-riverbank",                   "circle", "magenta"},
                {"relations without type",              "circle", "purple"},
                {"places of worship without religion",  "circle", "olive"},
                {"missing maxspeed",                    "circle", "gray"},
                {"waterway-waterway",                   "circle", "lime"},
                {"faintly connected",                   "circle", "maroon"},
                {"language unknown",                    "circle", "silver"},
                {"misspelled tags",                     "circle", "white"},
                {"highway-highway",                     "circle", "navy"},

                {"bridge-tags",                         "square", "red"},
                {"way without tags",                    "square", "green"},
                {"not closed loop",                     "square", "blue"},
                {"splitting boundary",                  "square", "cyan"},
                {"cyclew/footp-waterway",               "square", "yellow"},
                {"non-physical use of sport-tag",       "square", "teal"},
                {"missing tracktype",                   "square", "magenta"},
                {"dead-ended one-ways",                 "square", "purple"},
                {"motorways without ref",               "square", "olive"},
                {"doubled places",                      "square", "gray"},
                {"mixed layers intersections",          "square", "lime"},
                {"missing turn restriction",            "square", "maroon"},
                {"railway crossings without tag",       "square", "silver"},
                {"cyclew/footp-riverbank",              "square", "white"},
                {"missing admin_level",                 "square", "navy"},

                {"http error",                          "diamond", "red"},
                {"multiple nodes on the same spot",     "diamond", "green"},
                {"non-closed areas",                    "diamond", "blue"},
                {"wrongly used railway crossing tag",   "diamond", "cyan"},
                {"loopings",                            "diamond", "yellow"},
                {"strange layers",                      "diamond", "teal"},
                {"motorways connected directly",        "diamond", "magenta"},
                {"wrong restriction angle",             "diamond", "purple"},
                {"highway-cyclew/footp",                "diamond", "olive"},
                {"missing from way",                    "diamond", "gray"},
                {"missing name",                        "diamond", "lime"},
                {"missing to way",                      "diamond", "maroon"},
                {"name but no other tag",               "diamond", "silver"},
                {"tag combinations",                    "diamond", "white"},
                {"node without tags",                   "diamond", "navy"},

                {"highway-cycleway",                    "triangle", "red"},
                {"already restricted by oneway",        "triangle", "green"},
                {"impossible angles",                   "triangle", "blue"},
                {"cyclew/footp-cyclew/footp",           "triangle", "cyan"},
                {"via is not on the way ends",          "triangle", "yellow"},
                {"missing type",                        "triangle", "teal"},
                {"*_link-connections",                  "triangle", "magenta"},
                {"wrong direction",                     "triangle", "purple"},
                {"cycleway-cycleway",                   "triangle", "olive"},
                {"non-match",                           "triangle", "gray"},
                {"cycleway-riverbank",                  "triangle", "lime"},
                {" from or to not a way",               "triangle", "maroon"},
                {"empty tags",                          "triangle", "silver"},
                {"wrong direction of to member",        "triangle", "white"},
                {"ways without nodes",                  "triangle", "navy"},

                {"admin_level too high",                "cross", "red"},
                {"cycleway-waterway",                   "cross", "green"},
                {"domain hijacking",                    "cross", "blue"}
        };

        boolean found=false;

        for(int i=0; i<values.length; i++) {

            if (mErrorName.compareTo(values[i][0])==0) {

                if (values[i][1].compareTo("circle")==0) {

                    drawableId=R.drawable.error_circle;
                }
                else if (values[i][1].compareTo("square")==0) {

                    drawableId=R.drawable.error_square;
                }
                else if (values[i][1].compareTo("diamond")==0) {

                    drawableId=R.drawable.error_diamond;
                }
                else if (values[i][1].compareTo("triangle")==0) {

                    drawableId=R.drawable.error_triangle;
                }
                else if (values[i][1].compareTo("cross")==0) {

                    drawableId=R.drawable.error_cross;
                }
                else {

                    drawableId=R.drawable.error_circle;
                }

                try {
                    color=Color.parseColor(values[i][2]);
                }
                catch (IllegalArgumentException e) {

                    color=Color.parseColor("black");
                }

                found=true;

                break;
            }
        }

        if (!found) {

            drawableId=R.drawable.error_circle;
            color=Color.parseColor("black");
        }

        Drawable draw=res.getDrawable(drawableId);

        Drawable newDraw;

        if (draw instanceof GradientDrawable) {

            GradientDrawable gradDraw=(GradientDrawable) draw;

            newDraw=gradDraw.mutate().getConstantState().newDrawable();

            ((GradientDrawable)newDraw).setColor(color);
        }
        else if (draw instanceof VectorDrawable) {

            VectorDrawable vectorDraw=(VectorDrawable) draw;

            PorterDuffColorFilter pdcf=new PorterDuffColorFilter(color,
                    PorterDuff.Mode.MULTIPLY);

            newDraw=vectorDraw.mutate().getConstantState().newDrawable();

            newDraw.setColorFilter(pdcf);

            //BlendModeColorFilter bmcf=new BlendModeColorFilter(Color.YELLOW, BlendMode.MULTIPLY);

            //vectorDraw.mutate().setTint(Color.YELLOW);
        }
        else {

            color=Color.parseColor("black");

            newDraw=draw.mutate().getConstantState().newDrawable();
        }

        setIcon(newDraw);
    }
}
