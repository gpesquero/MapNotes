package osm.mapnotes.util;

public class StringUtils
{
  public static String flatString(String input)
  {
    return input.replace("&", "&amp;").replace("'", "&apos;");
  }
}
