/*
 * PropertyResourceBundle.java
 *
 * replica of java.util.PropertyResourceBundle
 * Created on February 14, 2005, 9:35 AM
 */

package com.varankin.mobile;

import java.io.*;
import java.lang.*;
import java.util.Hashtable;

/**
 * @author  Nikolai Varankine
 */
public class PropertyResourceBundle extends Hashtable
{
    private final String ext = ".properties";

    public PropertyResourceBundle( InputStream a_stream ) 
        throws IllegalArgumentException, IOException, UnsupportedEncodingException
    {
        super( 20 ); // estimated value
        stuff( a_stream );
    }

    public PropertyResourceBundle( String a_filename ) // not found in original class
        throws IllegalArgumentException, IOException, UnsupportedEncodingException
    {
        super( 20 ); // estimated value
        InputStream stream = getStream( a_filename, getLocale() );
        stuff( stream );
        stream.close();
    }

    public static String getLocale()
    {
        String locale, default_locale = "en";
        try { locale = System.getProperty( "microedition.locale" ); }
        catch ( NullPointerException e ) { locale = default_locale; }
        catch ( IllegalArgumentException e ) { locale = default_locale; }
        // locale = "ru-RU"; //DEBUG, CONSIDER GLOBAL SETUP
        return locale;
    }

    private InputStream getStream( String a_filename, String a_locale )
        throws IllegalArgumentException
    {
        String locale = a_locale != null ? "_" + a_locale : "";
        InputStream is = getClass().getResourceAsStream( a_filename + locale + ext );
        if( is == null ) 
        {
            locale = locale.replace( '-', '_' );
            is = getClass().getResourceAsStream( a_filename + locale + ext );
        }
        if( is == null && locale.lastIndexOf( '_' ) > 1 ) 
        {
            locale = locale.substring( 0, locale.lastIndexOf( '_' ) );
            is = getClass().getResourceAsStream( a_filename + locale + ext );
        }
        if( is == null ) 
        {
            is = getClass().getResourceAsStream( a_filename + ext );
        }
        return is;
    }

    /**
     * Called to read a_stream and put pairs key-value into hash table
     * 
     * stream format: KEY=VALUE<LF><LF>KEY=VALUE<LF><LF>...KEY=VALUE<LF>
     *
     * NOTE: it's been supposed that comments are absent in the file
     *
     */
    private void stuff( InputStream a_stream ) 
        throws IllegalArgumentException, UnsupportedEncodingException, IOException
    {
        byte[] buffer = new byte[512]; // DEBUG: temporary limits
        byte previous;
        String key = null, token = null;
        int bp = 0;
        
        if( a_stream == null ) throw new IllegalArgumentException();
        
        for( previous = 0x00; a_stream.read( buffer, bp, 1 ) > 0; bp++ ) 
        {
          switch( (int) buffer[bp] )
          {
            case 0x0A: // i.e. ASCII LF
            {
                if( true || buffer[bp] == previous ) // second terminator in between
                {
                    token = new String( buffer, 0, bp, "utf-8" );
                    if( key != null ) put( new StringBuffer ( key ), new StringBuffer ( token ) ); // pair is ready, save it
                    key = null;
                    token = null;
                    previous = buffer[bp];
                    bp = -1;
                }
                else // terminator at the end of value? or not...
                {
                    previous = buffer[bp];
                }
                break;
            }
            
            case 0x3D: // i.e. ASCII "="
            {
                previous = buffer[bp];
                key = new String( buffer, 0, bp, "utf-8" ); token = null;
                bp = -1;
                break;
            }
            
            default:
            {
                previous = buffer[bp];
            }
          }
          if( bp == buffer.length-1 )
          {
              byte[] replacement = new byte[ buffer.length*3/2 ];
              for( int b = 0; b < buffer.length; b++ ) replacement[ b ] = buffer[ b ];
              buffer = replacement;
          }
        }
        if( key != null ) put( new StringBuffer ( key ), new StringBuffer ( new String( buffer, 0, bp, "utf-8" ) ) ); // pair is ready, save it
    }
    
    /**
     * Called to convert \\uNNNN and \\C to single char
     */
    private StringBuffer utf( StringBuffer a_buffer )
    {
        final char slash = '\\';
        char[] nnnn = new char[4];
        
        for( int p = 0; p < a_buffer.length(); p++ )
            if( a_buffer.charAt( p ) == slash )
              if ( p + 6 <= a_buffer.length() && a_buffer.charAt( p + 1 ) == 'u' )
              {
                // get 4 digit UTF hex code
                a_buffer.getChars( p + 2, p + 6, nnnn, 0 );
                // convert code to single char and replace pattern
                a_buffer.setCharAt( p, (char) Integer.parseInt( new String( nnnn ), 16 ) );
                a_buffer.delete( p + 1, p + 6 );
              }
              else if( p + 1 <= a_buffer.length() )
              {
                // remove slash but keep/replace escaped character
                a_buffer.delete( p, p + 1 );
                switch( a_buffer.charAt( p ) )
                {
                    case 'n': a_buffer.setCharAt( p, '\n' ); break;
                    case 'r': a_buffer.setCharAt( p, '\r' ); break;
                    case 'f': a_buffer.setCharAt( p, '\f' ); break;
                    case 't': a_buffer.setCharAt( p, '\t' ); break;
                    case 'b': a_buffer.setCharAt( p, '\b' ); break;
                }
              }

        return a_buffer;
    }

    private void put( StringBuffer a_key, StringBuffer a_value )
    {
        // // cut 0x0A at the end
        // a_value.setLength( a_value.length() - 1 ); 
        // save pair
        put( new String( a_key ), new String ( utf( a_value ) ) );
        // invalidate buffers
        a_key.setLength( 0 );
        a_value.setLength( 0 );
    }
    
    /**
     * Returns value for the key
     */
    public final String getString( String a_key ) 
    {
        return (String) get( a_key );
    }

    /**
     * Returns value for the key
     */
    public final String getString( Object a_object, String a_key ) 
    {
        String class_name = a_object.getClass().getName(); // fully qualified class name
        return (String) get( class_name.substring( class_name.lastIndexOf( '.' ) + 1 ) + "." + a_key );
    }
}
