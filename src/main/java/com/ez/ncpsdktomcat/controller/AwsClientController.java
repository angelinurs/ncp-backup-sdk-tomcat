package com.ez.ncpsdktomcat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ez.ncpsdktomcat.config.ObjectStorageProps;
import com.ez.ncpsdktomcat.service.BackupComponent;
import com.ez.ncpsdktomcat.vo.LogMaterialVO;
import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/aws")
public class AwsClientController {
	@Autowired
	private BackupComponent backupComponent;

	
	@Autowired
	private ObjectStorageProps objectStorageProps;
	
	@GetMapping("/sync")
	public String doTask() {
		
		String job_of_dumpall_schema = dumpall_schema();
		String job_of_dumpall_logs = dumpall_logs();
		
		log.info(job_of_dumpall_schema);
		log.info(job_of_dumpall_logs);
		
		return String.format("job_of_dumpall_schema : %s <br /> job_of_dumpall_logs : %s <br />", job_of_dumpall_schema, job_of_dumpall_logs );
	}
	
	@GetMapping("/sync/additional")
	public String doSyncAdditional() {
		
		String job_of_syncAdditionalBuckets = syncAdditionalBuckets();
		
		log.info(job_of_syncAdditionalBuckets);
		
		return String.format("job_of_syncAll_additionalBuckets : %s <br />", job_of_syncAdditionalBuckets );
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
