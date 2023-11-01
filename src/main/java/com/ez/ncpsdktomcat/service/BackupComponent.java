package com.ez.ncpsdktomcat.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ez.ncpsdktomcat.common.FileUtils;
import com.ez.ncpsdktomcat.config.ObjectStorageProps;
import com.ez.ncpsdktomcat.vo.LogMaterialVO;
import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BackupComponent {
	
	@Autowired
	private ScriptComponent scriptComponent;
	
	@Autowired
	private LogCollector logCollector;
	
	@Autowired
	@Qualifier( value = "userJdbcTemplate" )
	private JdbcTemplate userJdbcTemplate;
	
	@Autowired
	@Qualifier( value = "portalJdbcTemplate")
	private JdbcTemplate portalJdbcTemplate;
	
	@Autowired
	private ObjectStorageProps objectStorageProps;
	
	private ObjectStorageS3 objectStorageS3;
	
	public List<TenencySchemaVO> dumpallDBSchema( String kind ) {
		/*
		 * portal schema - key_tbl
		 * idx, schema_name, key, date_modify, date_created 
		 */
		
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
			
			/*
			 * portal schema - key_tbl
			 * idx, schema_name, key, date_modify, date_created 
			 * String key = "naru";
			 */
			
			vo.setKey(key);
			
			String command = scriptComponent.doDumpSchemas( vo, kind );
			
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
	
	public List<LogMaterialVO> dumpallLogs( String key ) {
		
//		key = "naru";

		// inquiry list of logs
		String[] logs = null;	
//		logs = logCollector.getLogs( "/home/naru/temp/log_test", "text.log" );
		
		String logPath = "/app/logs";
		logs = logCollector.getLogs( logPath, ".log" );
		
		List<LogMaterialVO> logMaterialVOs = new ArrayList<>();
		
		log.info("============ Start Backup ================");
		
		// dump, compress, encrypt all schema 
		for( String logfile : logs ) {
			
			LogMaterialVO vo = new LogMaterialVO( logfile, key );

			if( ! vo.isValid() ) {
				continue;
			}
			
			logMaterialVOs.add( vo );
			
			log.info("# # == Start ==" );
			
			
			Instant startTime = Instant.now();
			
			String command = scriptComponent.doDumpLogs( logfile, key );
			
			Instant endTime = Instant.now();
			
			long diffTime = Duration.between(startTime, endTime).toMillis();
			
			StringBuilder sbStatus = new StringBuilder();
			sbStatus.append( String.format( "\n# # ======= %s %s- work table =======\n", vo.getTenentName(), vo.getFileName() ) )
					.append( String.format( "Start Time    : %s\n", startTime) )
					.append( String.format( "End   Time    : %s\n", endTime) )
					.append( String.format( "Duration      : %s\n", diffTime) )
//					.append( String.format( "shell command : %s\n", command) )
					.append( "# # == End ==" );
			
			log.info( sbStatus.toString() );
			
		}
		log.info("============= End Backup ================");
		
		return logMaterialVOs;
	} 

	// Tenent Application logs
	public void exportLogsToObjectStorage( List<LogMaterialVO> logMaterialVOs ) {
		
		objectStorageS3 = new ObjectStorageS3(objectStorageProps);

//		String schemaName = "psm_sc_svc171";
//		String filename = "psm_sc_svc171.2023-09-22T16:11:27.tar.gz.enc";
//		String filePath = String.format( "/home/naru/temp/temp/%s", filename );
		
//		filePath = "/home/naru/temp/temp/";
		String objectFolderName = "logs/";
		
		for( LogMaterialVO vo : logMaterialVOs ) {
			if( ! vo.isValid() ) {
				continue;
			}
			String schemaName = vo.getTenentName();
			String bucketName = vo.getTenentName();
			
			String objectName = vo.getFianlFileName();
			String sourceFile = vo.getFinalSourcePath();
			String objectPath = vo.getObjectPath();
			
			objectStorageS3.createBucket( bucketName );
			objectStorageS3.uploadObject( bucketName, objectFolderName, objectFolderName + objectPath + objectName, sourceFile );
			
			FileUtils.deleteLogFile( vo.getSourcePath() );
//			FileUtils.deleteLogFile( sourceFile );
//			FileUtils.deleteLogFile( sourceFile.replace(".gz", "") );
			
			StringBuilder sb = new StringBuilder();
			sb.append( String.format( "\n============================\n" ) )
			  .append( String.format( "+ Schema   name : %s\n", schemaName ) )
			  .append( String.format( "============================\n" ) )
			  .append( String.format( "+ Source   Path : %s\n", sourceFile ) )
			  .append( String.format( "+ Bucket   name : %s\n", bucketName ) )
			  .append( String.format( "+ Object   name : %s\n", objectName ) )
			  .append( String.format( "+ Object   Path : %s\n",  objectFolderName + objectPath + objectName ) )
			  .append( String.format( "============================\n" ) );
			
			log.error(sb.toString());
			
		}
	}
	
	// Tenent DB Schema
	public void exportSchemasToObjectStorage( List<TenencySchemaVO> schemaVOs ) {
		
		objectStorageS3 = new ObjectStorageS3(objectStorageProps);
		
//		String schemaName = "psm_sc_svc171";
//		String filename = "psm_sc_svc171.2023-09-22T16:11:27.tar.gz.enc";
//		String filePath = String.format( "/home/naru/temp/temp/%s", filename );
		
		for( TenencySchemaVO vo : schemaVOs ) {
			
			if( FileUtils.getFileExist( vo ) ) {
				
				StringBuilder sb = new StringBuilder();
				
				String schemaName = vo.getSchema();
				String bucketName = schemaName;
				String sourcefile = vo.getAbsolutePath();	 
				String objectFolderName = vo.getObjectPath();
				String objectName = objectFolderName + vo.getFileName();
				
				sb.append( "\n==========================================\n" )
				  .append( "Schema     name : " ).append( schemaName ).append("\n")
				  .append( "Bucket     name : " ).append( bucketName ).append("\n")
				  .append( "SourceFile name : " ).append( sourcefile ).append("\n")
				  .append( "Object     name : " ).append( objectName ).append("\n")
				  .append( "==========================================\n" );
				
				log.info( sb.toString() );
				
				objectStorageS3.createBucket( bucketName );
				objectStorageS3.uploadObject( bucketName, objectFolderName, objectName, sourcefile );
				FileUtils.deleteSchemaFile( vo );
				
			} else {
				continue;
			}
			
		}
	}
	
}
