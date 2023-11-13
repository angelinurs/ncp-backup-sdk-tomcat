package com.ez.ncpsdktomcat.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

/**
 * @author kyoung il pak
 * @version 0.9.0
 * @since 2023.11.12.mon
 * 
 * @apiNote
 * 
 * * method list <br />
 * {@link #getSchemaList() <br />
 * 
 */
public class DBService {
		
	private JdbcTemplate jdbcTemplate;
	
	public DBService( JdbcTemplate jdbcTemplate ) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @author kyoung il pak
	 * @implNote get schemeVO list for backup
	 * @return get schemeVO list
	 */
	public List<TenencySchemaVO> getSchemaList() {
		
	    List<TenencySchemaVO> results = null;
		
		StringBuilder sql = new StringBuilder();
		sql.append( " SELECT " )
		  .append( "   DISTINCT( sequence_schema ) as schema, current_date as date, current_time as time " )
		  .append( " FROM " )
		  .append( "   INFORMATION_SCHEMA.SEQUENCES " );
		
		results = jdbcTemplate.query( 
				sql.toString(),				
				(rs, rowNum) -> new TenencySchemaVO(
										rs.getString( "schema" ),
										rs.getString( "date" ),
										rs.getString( "time" )
										)
				);
		
		return results;
	}
}
