/*
 * RegistryRecord.java
 *
 * Created on February 11, 2005, 10:50 PM
 */

package com.varankin.mobile;

import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.lang.Integer;

/**
 * @author  Nikolai Varankine
 */
public class RegistryRecord
{
    private final static String ENCODING = "utf-8";
    private byte[] key; // 255 bytes max
    private byte[] value;

    public RegistryRecord( String a_key, byte[] a_value )
    {
        try { key = a_key.getBytes( ENCODING ); } // returns new array
        catch( UnsupportedEncodingException e ) { key = a_key.getBytes(); }
        /*
        value = new byte [ a_value.length ]; // making new array
        for( int b = 0; b < a_value.length; b++ ) value[ b ] = a_value[ b ];
        */
        value = a_value;
    }

    public RegistryRecord( String a_key, String a_value )
    {
        try { key = a_key.getBytes( ENCODING ); } // returns new array
        catch( UnsupportedEncodingException e ) { key = a_key.getBytes(); }
        try { value = a_value.getBytes( ENCODING ); } // returns new array
        catch( UnsupportedEncodingException e ) { value = a_value.getBytes(); }
    }

    public RegistryRecord( byte[] a_key_value )
    {
        int b, bkv = 2;
        int key_length = ( a_key_value[ 0 ] << 8 ) + a_key_value[ 1 ];
        int value_length = a_key_value.length - key_length - 2;
        key   = new byte[ key_length ];
        value = new byte[ value_length ];
        /*
        for( b = 0; b < key.length;   b++ )   key[ b ] = a_key_value[ bkv++ ];
        for( b = 0; b < value.length; b++ ) value[ b ] = a_key_value[ bkv++ ];
         */
        System.arraycopy( a_key_value, bkv, key, 0, key.length );
        System.arraycopy( a_key_value, bkv+key.length, value, 0, value.length );
    }
    
    public int getLength()
    {
        return 2 + key.length + value.length;
    }
    
    public byte[] getBytes()
    {
        byte[] rv = new byte[ getLength() ];
        int b, brv = 0;

        rv[ brv++ ] = (byte) ( key.length >> 8 );
        rv[ brv++ ] = (byte) key.length;
        /*
        for( b = 0; b < key.length;   b++ ) rv[ brv++ ] = key[ b ];
        for( b = 0; b < value.length; b++ ) rv[ brv++ ] = value[ b ];
         */
        System.arraycopy( key, 0, rv, brv, key.length );
        System.arraycopy( value, 0, rv, brv+key.length, value.length );

        return rv;
    }

    public String getKey()
    {
        try { return new String( key, ENCODING ); } 
        catch( UnsupportedEncodingException e ) { return new String( key ); }
    }
    
    public byte[] getByteValue()
    {
        byte[] rv = new byte [ value.length ];
        for( int b = 0; b < value.length; b++ ) rv[ b ] = value[ b ];
        return rv;
    }

    public String getStringValue()
    {
        try { return new String( value, ENCODING ); } 
        catch( UnsupportedEncodingException e ) { return new String( value ); }
    }

}
