package com.ez.ncpsdktomcat.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ez.ncpsdktomcat.config.ObjectStorageProps;
import com.ez.ncpsdktomcat.service.BackupComponent;
import com.ez.ncpsdktomcat.vo.LogMaterialVO;
import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BackupScheduler {
	
	@Autowired
	private BackupComponent backupComponent;
	
//	@Value("${application.object-storage.ADDITIONAL_BUCKETS}")    
//	private String[] additionalBuckets;
	
	@Autowired
	private ObjectStorageProps objectStorageProps;

//	@Scheduled(cron="0/10 * * * * *")
//	@Scheduled(cron="0 0 0 1 * * *")
	@Scheduled(cron="${application.etc.SCHEDULE_TIME}")
	public void backupTask() {
		
		String job_of_dumpall_schema = dumpall_schema();
		String job_of_dumpall_logs = dumpall_logs();
		String job_of_syncAdditionalBuckets = syncAdditionalBuckets();
		
		log.info(job_of_dumpall_schema);
		log.info(job_of_dumpall_logs);
		log.info(job_of_syncAdditionalBuckets);
	}

	public String dumpall_schema() {
		
		List<TenencySchemaVO> users = backupComponent.dumpallDBSchema( "user" );
		List<TenencySchemaVO> portals = backupComponent.dumpallDBSchema( "portal" );
		
		if( users != null ) {
			backupComponent.exportSchemasToObjectStorage( users );			
		}
		if( portals != null ) {
			backupComponent.exportSchemasToObjectStorage( portals );			
		}
		
		return "backup DB schema is done.";
	}

	public String dumpall_logs() {
		
		List<LogMaterialVO> logMaterialVOs = backupComponent.dumpallLogs();
		
		if( logMaterialVOs != null ) {
			backupComponent.exportLogsToObjectStorage( logMaterialVOs );			
		}
		
		return "backup logs is done.";
	}
	
	public String syncAdditionalBuckets() {
		
		List<String> additionalBuckets = objectStorageProps.getADDITIONAL_BUCKETS();
		
		String rtnMessage = "";
		
		if( additionalBuckets.isEmpty() ) {
			
			rtnMessage = "Additional Bucket is empty.";
			
		} else {
			
			backupComponent.syncToObjectStoragesWormBucket(additionalBuckets);
			
			rtnMessage = "Synchronization of additional buckets completed.";			
		}
		
		return rtnMessage;
	}
}
