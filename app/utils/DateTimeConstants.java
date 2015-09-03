package utils;

import java.text.SimpleDateFormat;

/**
 * Some global constants relating to date/time formatting.
 */
public class DateTimeConstants {
  
  public final static String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  public final static SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat(DATETIME_FORMAT);

}
