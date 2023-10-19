package com.ez.ncpsdktomcat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ez.ncpsdktomcat.common.ErrorLogMessage;
import com.ez.ncpsdktomcat.config.ObjectStorageProps;
import com.ez.ncpsdktomcat.deprecated.ObjectStorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ncp")
public class NCPObjectStorageController {
	
	private ObjectStorageService objectStorageService;
	
	@Autowired
	private ObjectStorageProps objectStorageProps;
	
	@GetMapping("/list" )
	public void getList() {
		 String queryString = "max-keys=10&delimiter=/";
		 String bucketName = "worm-db-bucket";
		 
		 objectStorageService = new ObjectStorageService( objectStorageProps );
		 
	     try {
			objectStorageService.listObjects( bucketName, queryString);
		 } catch (Exception e) {
			 
//			log.error("{}", getPrintStackTrace( e ) );	
			log.error("{}", ErrorLogMessage.getPrintStackTrace( e ) );	
		 }
	}
	
	@GetMapping("/list/{bucketName}" )
	public void getList( @PathVariable String bucketName ) {
		String queryString = "max-keys=10&delimiter=/";
		
		objectStorageService = new ObjectStorageService( objectStorageProps );
				
		try {
			objectStorageService.listObjects( bucketName, queryString);
		} catch (Exception e) {
			
			log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );	
		}
	}
	
	@GetMapping("/prop")
	public String getProp() {
		StringBuilder sb = new StringBuilder();
		sb.append("CHARSET_NAME : ").append(  objectStorageProps.getCHARSET_NAME()).append("<br />")
		  .append("HMAC_ALGORITHM : ").append( objectStorageProps.getHMAC_ALGORITHM()).append("<br />")
		  .append("HASH_ALGORITHM : ").append( objectStorageProps.getHASH_ALGORITHM()).append("<br />")
		  .append("AWS_ALGORITHM : ").append( objectStorageProps.getAWS_ALGORITHM()).append("<br />")
		  .append("SERVICE_NAME : ").append( objectStorageProps.getSERVICE_NAME()).append("<br />")
		  .append("REQUEST_TYPE : ").append( objectStorageProps.getREQUEST_TYPE()).append("<br />")
		  .append("UNSIGNED_PAYLOAD : ").append( objectStorageProps.getUNSIGNED_PAYLOAD() ).append("<br />")
		  .append("REGION_NAME : ").append( objectStorageProps.getREGION_NAME( )).append("<br />")
		  .append("ENDPOINT : ").append( objectStorageProps.getENDPOINT() ).append("<br />")
		  .append("ACCESS_KEY : ").append( objectStorageProps.getACCESS_KEY() ).append("<br />")
		  .append("SECRET_KEY : ").append( objectStorageProps.getSECRET_KEY() ).append("<br />");
		
		return sb.toString();
	}
	
}
