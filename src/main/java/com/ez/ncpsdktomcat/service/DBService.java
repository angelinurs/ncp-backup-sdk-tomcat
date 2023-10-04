package com.ez.ncpsdktomcat.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DBService {
		
	private JdbcTemplate jdbcTemplate;
	
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
										rs.getString( "time" ),
										0
										)
				);
		
		return results;

	}

}
