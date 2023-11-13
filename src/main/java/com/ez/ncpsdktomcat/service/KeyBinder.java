package com.ez.ncpsdktomcat.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ez.ncpsdktomcat.common.ErrorLogMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author kyoung il pak
 * @version 0.9.0
 * @since 2023.11.12.mon
 * 
 * @apiNote
 * 
 * * method list <br />
 * {@link #getKey(String) <br />
 * 
 */
@Slf4j
public class KeyBinder {
	
	private JdbcTemplate jdbcTemplate;
	
	public KeyBinder( JdbcTemplate jdbcTemplate ) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote get encrypt key as scheme
	 * @return get key
	 */
	public String getKey( String schemaName ) {
		
		StringBuilder sb = new StringBuilder();
		sb.append( " SELECT value " )
		  .append( " FROM psm_schema.key_table " )
		  .append( String.format( " WHERE profile=$$%s$$ ", schemaName ) )
		  .append( " ORDER BY seq DESC " ).append( "\n" )
		  .append( " LIMIT 1 " );
		
		log.info( sb.toString() );

	    String key = null; 
	    
	    
        try {
        	
        	key = jdbcTemplate.queryForObject( sb.toString(), String.class );
        	
        } catch ( EmptyResultDataAccessException e ) {
        	
        	key = schemaName;
        	log.error( ErrorLogMessage.getPrintStackTrace(e) );
        	
        } finally {

    	    if( key.isBlank() ) key = schemaName;
		}
        
        return key;

	}
	
}
