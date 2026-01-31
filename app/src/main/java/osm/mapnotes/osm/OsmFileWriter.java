package osm.mapnotes.osm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class OsmFileWriter
{
  private FileWriter mFileWriter;
  private String mErrorString = "None";

  private final ArrayList<OsmNode> mNodes = new ArrayList<>();

  public String getErrorString()
  {
    return mErrorString;
  }

  public boolean createNewFile(String fileName)
  {
    try
    {
      mFileWriter = new FileWriter(fileName);
    }
    catch (IOException e)
    {
      mFileWriter = null;
      mErrorString = e.getMessage();
      return false;
    }

    return true;
  }

  public boolean writeFile()
  {
    if (mFileWriter == null)
      return false;

    try
    {
      // Write OSM file header.
      mFileWriter.write("<?xml version='1.0' encoding='UTF-8'?>\n");
      mFileWriter.write("<osm version='0.6' generator='MapNotes'>\n");

      // Write nodes.
      Iterator<OsmNode> osmNodeIterator = mNodes.iterator();
      long nodeId = -1;
      while(osmNodeIterator.hasNext())
      {
        OsmNode node = osmNodeIterator.next();

        HashMap<String, String> nodeTags = node.getTags();

        // Write node basic info.
        mFileWriter.write(String.format(Locale.US,
                                        "  <node id='%d' timestamp='%s' lat='%.07f' lon='%.07f'",
                                        nodeId--, node.mTimeStamp, node.mLat, node.mLon) +
                          (nodeTags.isEmpty()? "/>" : ">") + "\n");

        // Write node tags (if any).
        for (String key : nodeTags.keySet())
        {
          mFileWriter.write(String.format(Locale.US,
                                          "    <tag k='%s' v='%s'/>\n",
                                          key, nodeTags.get(key)));
        }

        // Write node closure (if applicable).
        if (!nodeTags.isEmpty())
          mFileWriter.write("  </node>\n");
      }

      // Write OSM file ending.
      mFileWriter.write("</osm>\n");

      // Close OSM file.
      mFileWriter.close();
    }
    catch (IOException e)
    {
      mErrorString = e.getMessage();
      return false;
    }

    return true;
  }

  public boolean close()
  {
    try
    {
      // Close file.
      mFileWriter.close();
    }
    catch (IOException e)
    {
      mErrorString = e.getMessage();
      return false;
    }

    return true;
  }

  public void addNode(OsmNode node)
  {
    mNodes.add(node);
  }
}
