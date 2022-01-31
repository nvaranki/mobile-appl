/*
 * Dispatcher.java
 *
 * Created on February 15, 2005, 10:50 PM
 */

package com.varankin.mobile;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

/**
 * @author  Nikolai Varankine
 */
public abstract class Dispatcher extends MIDlet
{
    public final static String RKEY_SERVER = "SERVER"; // <IP address>:<TCP/IP port no.>
    public final static String RKEY_HELP   = "HELP"; // help mode
    public final static String RKEY_MSISDN = "MSISDN"; // mobile subscriber ID
    public final static String RKEY_EXPIRES = "EXPIRES"; // expiration date
    public final static String RKEY_PASS = "PWRD"; // registration code
    public final static String RKEY_INIT = "INIT"; // activation ID
    public final static int HELP_NO = 0;
    public final static int HELP_TICKER = 1;
    public final static int HELP_COMMAND = 2;

    public Display display;
    public Registry registry;
    public PropertyResourceBundle messages;
    public Runtime runtime;

    private Displayable current;
    private int m_help;
    
    public Dispatcher( long a_evaluation ) throws IOException, RecordStoreException
    {
        super();
        display = Display.getDisplay( this );
        registry = new Registry( getPackageName( this ) );
        runtime = Runtime.getRuntime();
        
        // load/update mandatory values
        if( ! registry.contains( RKEY_EXPIRES ) )
            try { setDeadline( this, getDeadline( this, a_evaluation ) ); }
            catch( RecordStoreException e ) { exitRequested(); }
        if( ! registry.contains( RKEY_INIT ) )
            try { registry.setValue( RKEY_INIT, generateId() ); }
            catch( RecordStoreException e ) { exitRequested(); }
        
        // setup localized messages
        messages = new PropertyResourceBundle( getClassPath( this ) + "res/messages" );
        m_help = getHelpMode( this );
    }

    // calls to Display.getDisplay(this).setCurrent(...)
    public void setCurrent()
    {
        display.setCurrent( current );
    }
    public void setCurrent( Displayable a_displayable )
    {
        display.setCurrent( a_displayable );
        if( a_displayable != null ) { current = a_displayable; }
        else { notifyPaused(); }
    }
    public void setCurrent( Alert a_alert, Displayable a_displayable )
    {
        if( a_alert != null ) { display.setCurrent( a_alert, a_displayable ); current = a_displayable; }
        else setCurrent( a_displayable );
    }
    public void setCurrent( AlertType a_alert_type, int a_alert_index, Displayable a_callback, Exception a_exception, Object a_owner )
    {
        String extra_comment = a_exception != null ? a_exception.getMessage() : null; //.toString()
        extra_comment = extra_comment != null ? ("\n"+extra_comment) : "";
        String alert = "Alert.";
        if( a_alert_index >= 0 ) alert += String.valueOf( a_alert_index ) + ".";
        display.setCurrent( new Alert( 
            getString( a_owner, alert+"Title" ), 
            getString( a_owner, alert+"Message" ) + extra_comment, 
            null, a_alert_type ), a_callback );
    }
    public void setCurrent( AlertType a_alert_type, int a_alert_index, Displayable a_callback, Exception a_exception )
    {
        setCurrent( a_alert_type, a_alert_index, a_callback, a_exception, a_callback );
    }

    /**
     * Returns Copy ID setting from RMS.
     */
    public static String getCopyID( Dispatcher a_dispatcher )
    {
        return a_dispatcher.registry.getValue( RKEY_INIT );
    }
    /**
     * Makes pseudo-unique Copy ID of running software copy.
     */
    private String generateId()
    {
        int mr = Character.MAX_RADIX;
        long timestamp = ( new Date() ).getTime();
        long modifier = Math.abs( ( new java.util.Random() ).nextLong() );
        final String dot = ".";
        String rv = 
              getAppProperty( "MIDlet-Name" ) + dot 
            + getAppProperty( "MIDlet-Version" ) + dot
            + Long.toString( timestamp, mr-- ).toUpperCase() + dot
            + Long.toString( modifier, mr-- ).toUpperCase() + dot;
        return rv + Integer.toString( Math.abs( rv.hashCode() ), mr ).toUpperCase();
    }

    /**
     * Returns Help Mode setting from RMS.
     */
    public static int getHelpMode( Dispatcher a_dispatcher )
    {
        String rgstr = a_dispatcher.registry.getValue( RKEY_HELP );
        try { return rgstr == null ? HELP_TICKER : Integer.parseInt( rgstr ); }
        catch( NumberFormatException e ) { return HELP_TICKER; }
    }
    /**
     * Saves supplied Help Mode setting into RMS.
     */
    public static void setHelpMode( Dispatcher a_dispatcher, int a_value )
        throws RecordStoreException
    {
        a_dispatcher.registry.setValue( RKEY_HELP, String.valueOf( a_value ) );
    }
    /**
     * Returns current Help Mode setting.
     */
    public final int getHelpMode()
    {
        return m_help;
    }
    /**
     * Sets and saves into RMS supplied Help Mode setting.
     */
    public final void setHelpMode( int a_new_mode )
    {
        try { setHelpMode( this, m_help = a_new_mode ); }
        catch( RecordStoreException e ) {} // recoverable problem
    }

    
    /**
     * Returns relaxed Deadline setting from RMS.
     */
    protected static long getDeadline( Dispatcher a_dispatcher, long a_allowance )
    {
        long estimation = ( new Date() ).getTime() + a_allowance;
        String rgstr = a_dispatcher.registry.getValue( RKEY_EXPIRES );
        try { return rgstr == null ? estimation : Long.parseLong( rgstr ); }
        catch( NumberFormatException e ) { return estimation; }
    }
    /**
     * Returns Deadline setting from RMS.
     */
    public static long getDeadline( Dispatcher a_dispatcher )
    {
        return getDeadline( a_dispatcher, 0L );
    }
    /**
     * Saves supplied Deadline setting into RMS.
     */
    public static void setDeadline( Dispatcher a_dispatcher, long a_value )
        throws RecordStoreException
    {
        a_dispatcher.registry.setValue( RKEY_EXPIRES, String.valueOf( a_value ) );
    }
    
    // calls to localized string repository
    public String getString( String a_subkey )
    {
        return messages.getString( a_subkey );
    }
    public String getString( Object a_object, String a_subkey )
    {
        return messages.getString( a_object, a_subkey );
    }

    public String getServerURL( Class a_class )
    {
       return getServerURL( "/"  + a_class.getName().replace('.', '/') ); //+ "/" );
    }
    public String getServerURL( Object a_object )
    {
       return getServerURL( getAppProperty( "Server-Address-" + getClassName( a_object ) ) );
    }
    public String getServerURL( String a_path )
    {
        return "//" + registry.getValue( RKEY_SERVER ) + a_path;
    }
    
    /**
     * Returns path to class object in JAR file, for example,
     * "/com/varankin/mobile/" for "com.varankin.mobile.Dispatcher".
     */
    public static String getClassPath( Object a_object )
    {
        String full_name = "/" + a_object.getClass().getName().replace( '.', '/' );
        return full_name.substring( 0, full_name.lastIndexOf( '/' ) ) + "/";
    }
    /**
     * Returns short class name without package name, for example,
     * "Dispatcher" for "com.varankin.mobile.Dispatcher".
     */
    public static String getClassName( Object a_object )
    {
        String full_name = a_object.getClass().getName();
        return full_name.substring( full_name.lastIndexOf( '.' ) + 1 );
    }
    /**
     * Returns short class name from full class name, for example,
     * "mobile" from "com.varankin.mobile.Dispatcher".
     */
    public static String getPackageName( Object a_object )
    {
        String full_name = "." + a_object.getClass().getName();
        full_name = full_name.substring( 0, full_name.lastIndexOf( '.' ) );
        full_name = full_name.substring( full_name.lastIndexOf( '.' ) + 1 );
        return full_name;
    }

    // formats date/time acceptably
    public static String formatDateTime( String a_date_as_string )
    {
        return formatDateTime( Long.parseLong( a_date_as_string ) );
    }
    public static String formatDateTime( long a_date_as_long )
    {
        Calendar cl = Calendar.getInstance();
        cl.setTime( new Date( a_date_as_long ) );
        String minutes = "0" + String.valueOf( cl.get( Calendar.MINUTE ) );
        return  String.valueOf( cl.get( Calendar.DAY_OF_MONTH ) ) + "." + 
                String.valueOf( cl.get( Calendar.MONTH ) + 1 ) + "." + // January == 0
                String.valueOf( cl.get( Calendar.YEAR ) ) + " " + 
                String.valueOf( cl.get( Calendar.HOUR_OF_DAY ) ) + ":" + 
                minutes.substring( minutes.length() - 2 ) /*+ " GMT"*/;
    }

    public boolean isMIDletValid( int a_jar_size )
    {
        return Integer.parseInt( getAppProperty( "MIDlet-Jar-Size" ) ) == a_jar_size;
    }
    
    public boolean isLicenseValid()
    {
        return ( new Date() ).getTime() <= getDeadline( this );
    }

    public abstract void exitRequested();

}
