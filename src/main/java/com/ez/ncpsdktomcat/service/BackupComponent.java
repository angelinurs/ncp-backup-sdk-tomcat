package com.ez.ncpsdktomcat.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.model.Bucket;
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
	@Qualifier( value = "userJdbcTemplate" )
	private JdbcTemplate userJdbcTemplate;
	
	@Autowired
	@Qualifier( value = "portalJdbcTemplate")
	private JdbcTemplate portalJdbcTemplate;
	
	@Autowired
	private ObjectStorageProps objectStorageProps;
	
	// encrypt key
	private KeyBinder keyBinder;
	
	// java sdk
	private ObjectStorageS3 objectStorageS3;
	
	// aws client v2
	private AwsClientService awsClientService;
	
	public List<TenencySchemaVO> dumpallDBSchema( String kind ) {
		/*
		 * portal schema - key_tbl
		 * idx, schema_name, key, date_modify, date_created
		 * String key = "naru";
		 */
		
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
			 * key table in portal	
			 */
			
			keyBinder = new KeyBinder( portalJdbcTemplate );
			String key = keyBinder.getKey( vo.getProfile() );
			
			
			log.info( "key binder test : {}", String.format("vo.getProfile() : %s \nkey : %s", vo.getProfile(), key));
			
			vo.setKey(key);
			
			String command = scriptComponent.doDumpSchemas( vo, kind );
			
			log.error("\n=========================");
			log.error("\n command : {}", command );
			log.error("\n=========================");
			
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
	
	public List<LogMaterialVO> dumpallLogs() {

		// inquiry list of logs
		String[] logs = null;	
		String[] appLogs = null;	
		String[] dbLogs = null;	
//		logs = logCollector.getLogs( "/home/naru/temp/log_test", "text.log" );
		
		String appLogPath = "/app/logs";
		String dbLogPath = "/var/lib/postgresql/logs";
		
		appLogs = LogCollector.getLogs( appLogPath, ".log" );
		dbLogs = LogCollector.getLogs( dbLogPath, ".log" );
		
//		if( appLogs != null && dbLogs == null ) {
//			logs = appLogs;
//		} else if( appLogs == null && dbLogs != null ) {
//			logs = dbLogs;
//		} else if( appLogs != null && dbLogs != null ) {
//			logs = FileUtils.concatenate(appLogs, dbLogs);
//		}
		
		logs = ( appLogs != null && dbLogs == null )? appLogs :
			   ( appLogs == null && dbLogs != null )? dbLogs :
			   ( appLogs != null && dbLogs != null )? FileUtils.concatenate(appLogs, dbLogs):
				                                      null;
				   
		
		if( logs == null || logs.length == 0 ) {
			return null;
		}
		
		List<LogMaterialVO> logMaterialVOs = new ArrayList<>();
		
		log.info("============ Start Backup ================");
		
		// dump, compress, encrypt all schema 
		for( String logfile : logs ) {
			
			LogMaterialVO vo = new LogMaterialVO( logfile );

			if( ! vo.isValid() ) {
				continue;
			}
			
			keyBinder = new KeyBinder( portalJdbcTemplate );

//			String key = "naru";
			String key = keyBinder.getKey( vo.getProfile() );
			
			vo.setKey( key );
			
			logMaterialVOs.add( vo );
			
			log.info("# # == Start ==" );
			
			
			Instant startTime = Instant.now();
			
			String command = scriptComponent.doDumpLogs( logfile, key );
			
			log.error("\n=========================");
			log.error("\n command : {}", command );
			log.error("\n=========================");
			
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
			if(  vo.isValid() ) {
				
				String schemaName = vo.getTenentName();
				String bucketName = vo.getTenentName();
				
				String objectName = vo.getFianlFileName();
				String sourceFile = vo.getFinalSourcePath();
				String objectPath = vo.getObjectPath();
				
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
				
				objectStorageS3.createBucket( bucketName );
				objectStorageS3.uploadObject( bucketName, objectFolderName, objectFolderName + objectPath + objectName, sourceFile );
				
				FileUtils.deleteLogFile( vo.getSourcePath() );
				FileUtils.deleteLogFile( vo.getFinalSourcePath() );
//				FileUtils.deleteLogFile( sourceFile );
//				FileUtils.deleteLogFile( sourceFile.replace(".gz", "") );
				
//				if(isWormBucketList( bucketName + "-worm" ) ) {
//					
//					// syncBucket
//					syncBuckets( bucketName );
//				}
			}
			
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
				
				if(isWormBucketList( bucketName + "-worm" ) ) {
					
					// syncBucket
					syncBuckets( bucketName );
				}
			} 
			
		}
	}

	// check worm bucket exist
	private boolean isWormBucketList( String wormBucketName ) {
		
		boolean isBucket = false;
		
		objectStorageS3 = new ObjectStorageS3(objectStorageProps);
		List<Bucket> buckets = objectStorageS3.getBucketList();

		for( Bucket bucket : buckets ) {
			String bucketName = bucket.getName();
			
			if( wormBucketName.equals( bucketName ) ) {
				isBucket = true;
			}
		}
		
		return isBucket;
	}
	
	// do aws client sync bucket
	private void syncBuckets( String bucketName ) {
		String bucketNameWorm = bucketName + "-worm";
		
		awsClientService = new AwsClientService(objectStorageProps);
		
		String command = awsClientService.getCommand( bucketName, bucketNameWorm );
		List<String> envs = awsClientService.getAccessKeyEnvs();
		
		scriptComponent.RunScript( envs.toArray( new String[ envs.size() ]), 
				command.split( " " ), 
				null, 
				bucketNameWorm 
				);
		
		log.info( "=============================================" );
		log.info( "=== command [ {}  ] !! ===", command );
		log.info( "=============================================" );
		
	}
	
	
}
