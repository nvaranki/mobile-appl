/*
 * Registry.java
 *
 * Created on February 11, 2005, 7:12 PM
 */

package com.varankin.mobile;

import java.lang.String;
import java.util.Hashtable;
import javax.microedition.rms.*;

/**
 * @author  Nikolai Varankine
 */
public class Registry 
{
    private final static int MISSED = Integer.MIN_VALUE;
    private RecordStore rs;
    Hashtable ht;
    
    public Registry( String a_name ) throws RecordStoreException
    {
        rs = RecordStore.openRecordStore( a_name, true );

        // NEW CODE:
        // stuff hash table using whole database
        ht = new Hashtable( 100 );
        RecordEnumeration re = rs.enumerateRecords( null, null, true );
        while( re.hasNextElement() )
        {
            try
            {
                int id = re.nextRecordId();
                RegistryRecord rr = new RegistryRecord( rs.getRecord( id ) );
                ht.put( rr.getKey(), new Integer( id ) );
            }
            catch( InvalidRecordIDException e ) 
                { throw new RecordStoreException( "hash table error" ); }
        };
    }

    public void closeRecordStore()
    {
        try { if( rs != null ) rs.closeRecordStore(); }
        catch( RecordStoreNotOpenException e ) {}
        catch( RecordStoreException e ) {}
        rs = null;
        // NEW CODE:
        ht = null;
    }
    
    public int addRecord( String a_key, byte[] a_data )
        throws RecordStoreNotOpenException,
               RecordStoreException,
               RecordStoreFullException
    {
        RegistryRecord rr = new RegistryRecord( a_key, a_data );
        int idx = rs.addRecord( rr.getBytes(), 0, rr.getLength() );
        ht.put( a_key, new Integer( idx ) ); // NEW CODE
        return idx;
    }

    public int addRecord( String a_key, String a_data )
        throws RecordStoreNotOpenException,
               RecordStoreException,
               RecordStoreFullException
    {
        RegistryRecord rr = new RegistryRecord( a_key, a_data );
        int idx = rs.addRecord( rr.getBytes(), 0, rr.getLength() );
        ht.put( a_key, new Integer( idx ) ); // NEW CODE
        return idx;
    }

    public boolean contains( String a_key )
    {
        /*
        try{ return findKey( a_key ) != MISSED; }
        catch( Exception e ) { return false; }
         */
        // NEW CODE:
        return ht.containsKey( a_key );
    }

    private int findKey( String a_key )
        throws RecordStoreNotOpenException, RecordStoreException, InvalidRecordIDException
    {
        /*
        RecordEnumeration re = rs.enumerateRecords( null, null, true );
        while( re.hasNextElement() )
        {
            try
            {
                int id = re.nextRecordId();
                RegistryRecord rr = new RegistryRecord( rs.getRecord( id ) );
                if( rr.getKey().compareTo( a_key ) == 0 ) return id;
            }
            catch( InvalidRecordIDException e ) {}
        };
        //throw new InvalidRecordIDException( "not found" );
        return MISSED;
         */
        // NEW CODE:
        Object idx = ht.get( a_key );
        return idx != null ? ( (Integer)idx ).intValue() : MISSED;
    }
    
    public void deleteKey( String a_key )
        throws RecordStoreNotOpenException,
               RecordStoreException,
               RecordStoreFullException
    {
        try
        {
            int idx = findKey( a_key );
            if( idx != MISSED ) rs.deleteRecord( idx );
        }
        catch( InvalidRecordIDException e ) {}
    }
    
    public void setValue( String a_key, String a_data )
        throws RecordStoreNotOpenException,
               RecordStoreException,
               RecordStoreFullException
    {
        int idx = findKey( a_key );
        if( idx == MISSED ) addRecord( a_key, a_data );
        else 
        {
            RegistryRecord rr = new RegistryRecord( a_key, a_data );
            rs.setRecord( idx, rr.getBytes(), 0, rr.getLength() );
        }
    }

    public void setValue( String a_key, byte[] a_data )
        throws RecordStoreNotOpenException,
               RecordStoreException,
               RecordStoreFullException
    {
        int idx = findKey( a_key );
        if( idx == MISSED ) addRecord( a_key, a_data );
        else 
        {
            RegistryRecord rr = new RegistryRecord( a_key, a_data );
            rs.setRecord( idx, rr.getBytes(), 0, rr.getLength() );
        }
    }

    public String getValue( String a_key ) 
    {
        try
        {
            int idx = findKey( a_key );
            return idx == MISSED ? null 
                : ( new RegistryRecord( rs.getRecord( idx ) ) ).getStringValue();
        }
        catch( RecordStoreNotOpenException e ) { return null; }
        catch( InvalidRecordIDException e ) { return null; }
        catch( RecordStoreException e ) { return null; }
    }

    public byte[] getBinaryValue( String a_key ) 
    {
        try
        {
            int idx = findKey( a_key );
            return idx == MISSED ? null 
                : ( new RegistryRecord( rs.getRecord( idx ) ) ).getByteValue();
        }
        catch( RecordStoreNotOpenException e ) { return null; }
        catch( InvalidRecordIDException e ) { return null; }
        catch( RecordStoreException e ) { return null; }
    }
    
}
