package com.ez.ncpsdktomcat.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ez.ncpsdktomcat.common.FileUtils;
import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BackupComponent {
	
	@Autowired
	private PsqlClientComponent psqlClientComponent;
	
	@Autowired
	@Qualifier( value = "userJdbcTemplate" )
	private JdbcTemplate userJdbcTemplate;
	
	@Autowired
	@Qualifier( value = "portalJdbcTemplate")
	private JdbcTemplate portalJdbcTemplate;
	
	@Autowired
	private ObjectStorageS3 objectStorageS3;
	
	public List<TenencySchemaVO> dumpall( String kind ) {
		String key = "naru";		
//		String kind = "user";
		
		JdbcTemplate jdbcTemplate = null;
		
		switch( kind ) {
		case "user":
			jdbcTemplate = userJdbcTemplate;			
			break;
			
		case "portal":
			jdbcTemplate = portalJdbcTemplate;			
			break;
			
		}
		
		if( jdbcTemplate == null ) {
			return null;
		}
		
		// inquiry list of schema
		List<TenencySchemaVO> results = ( new DBService( jdbcTemplate )).getSchemaList();
		
		log.info("============ Start Backup ================");
		
		// dump, compress, encrypt all schema 
		for( TenencySchemaVO vo : results ) {
			
			log.info("# # == Start ==" );
			
			Instant startTime = Instant.now();
			
			String time = vo.getTime().split( "[.]" )[0];
			
			String command = psqlClientComponent.doDump( vo.getSchema(), vo.getDate(), time, kind, key );
			
			Instant endTime = Instant.now();
			
			long diffTime = Duration.between(startTime, endTime).toMillis();
			
			StringBuilder sbStatus = new StringBuilder();
			sbStatus.append( String.format( "======= %s - work table =======\n", vo.getSchema() ) )
			  		.append( String.format( "Start Time    : %s\n", startTime) )
			  		.append( String.format( "End   Time    : %s\n", endTime) )
			  		.append( String.format( "Duration      : %s\n", diffTime) )
//					.append( String.format( "shell command : %s\n", command) )
					.append( "# # == End ==" );
			
			log.info( sbStatus.toString() );
			
		}
		log.info("============= End Backup ================");
		
		return results;
	} 
	
	public void exportToObjectStorage( List<TenencySchemaVO> schemaVOs ) {

//		String schemaName = "psm_sc_svc171";
//		String filename = "psm_sc_svc171.2023-09-22T16:11:27.tar.gz.enc";
//		String filePath = String.format( "/home/naru/temp/temp/%s", filename );
		
		String filePath = "/home/naru/temp/temp/";
		String objectFolderName = "schemas/";
		
		for( TenencySchemaVO vo : schemaVOs ) {
			String schemaName = vo.getSchema();
			String bucketName = schemaName.replace( "_", "-" );
			
			String filename = FileUtils.getFileList( filePath, schemaName );			 
			String objectName = filename;			
			
			objectStorageS3.createBucket( bucketName );
			objectStorageS3.uploadObject( bucketName, objectFolderName, objectFolderName+objectName, filePath + filename );
			FileUtils.deleteFile(filePath, schemaName);
			
		}
	}
	
}
