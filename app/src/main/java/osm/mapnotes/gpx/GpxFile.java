package osm.mapnotes.gpx;

import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GpxFile {

    private final ArrayList<Polyline> mSegments = new ArrayList<>();

    Polyline mCurrentSegment=null;

    public GpxFile() {
    }

    public boolean openFile(String fileName) {

        try {
            DocumentBuilder dBuilder=DocumentBuilderFactory.newInstance().newDocumentBuilder();

            InputStream is=new FileInputStream(fileName);

            Document doc=dBuilder.parse(is);

            //String text=doc.getDocumentElement().getNodeName();

            if (doc.hasChildNodes()) {

                parseNodes(doc.getChildNodes());
            }

        }
        catch (ParserConfigurationException | IOException | SAXException e) {

            return false;
        }

        String text;

        text="Number of segments: "+mSegments.size();
        System.out.println(text);

        Iterator<Polyline> iter=mSegments.iterator();

        int count=0;

        while(iter.hasNext()) {

            Polyline line = iter.next();

            text = "Segment #"+count+" has "+line.getActualPoints().size()+" points";

            System.out.println(text);
        }

        return true;
    }

    private void parseNodes(NodeList nodeList) {

        for (int count=0; count<nodeList.getLength(); count++) {

            Node node = nodeList.item(count);

            // make sure it's element node.
            if (node.getNodeType()==Node.ELEMENT_NODE) {

                String nodeName=node.getNodeName();

                //System.out.println("Node Value =" + tempNode.getTextContent());

                if (nodeName.compareTo("trkseg")==0) {

                    // This is a new segment
                    mCurrentSegment=new Polyline();

                    Paint paint=mCurrentSegment.getOutlinePaint();
                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(12);
                }
                else if (nodeName.compareTo("trkpt")==0) {

                    // This is a point
                    if (mCurrentSegment==null) {
                        continue;
                    }

                    Double lon=null;
                    Double lat=null;

                    if (node.hasAttributes()) {

                        // get attributes names and values
                        NamedNodeMap nodeMap=node.getAttributes();

                        for (int i=0; i<nodeMap.getLength(); i++) {

                            Node attrNode=nodeMap.item(i);

                            String attrNodeName=attrNode.getNodeName();
                            String attrNodeValue=attrNode.getNodeValue();

                            if (attrNodeName.compareTo("lat")==0) {

                                lat=Double.parseDouble(attrNodeValue);
                            }
                            else if (attrNodeName.compareTo("lon")==0) {

                                lon=Double.parseDouble(attrNodeValue);
                            }
                        }
                    }

                    if ((lon!=null) && (lat!=null)) {

                        GeoPoint point=new GeoPoint(lat, lon);

                        mCurrentSegment.addPoint(point);
                    }
                }

                if (node.hasChildNodes()) {

                    // loop again if has child nodes
                    parseNodes(node.getChildNodes());
                }

                if (nodeName.compareTo("trkseg")==0) {

                    // We're closing a segment

                    mSegments.add(mCurrentSegment);

                    mCurrentSegment=null;
                }

                //System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
            }
        }
    }

    public ArrayList<Polyline> getPolylines() {

        return mSegments;
    }
}
