package com.ez.ncpsdktomcat.controller;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ez.ncpsdktomcat.config.ObjectStorageProps;

@RestController
@RequestMapping("/enc")
public class JasyptObjectStorageController {
	
	@Autowired
	private ObjectStorageProps objectStorageProps;
	
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
	
	@GetMapping("/jasypt/en")
	public String enJasypt() {
		String driverclassname = "org.postgresql.Driver";
		
		String adminJjdbcUrl  = "jdbc:postgresql://pg-1f3ar.vpc-cdb-kr.gov-ntruss.com:5432/psm_ez?charSet=ko_KR.UTF8";
		String adminUsername  = "psm_ez";
		String adminPassword = "dlwltjxl1!";
        String adminPoolName = "psm-admin-backup-pool";
        
		String userJdbcUrl  = "jdbc:postgresql://pg-1f3ar.vpc-cdb-kr.gov-ntruss.com:5432/psm_1?charSet=ko_KR.UTF8";
		String userUsername = "psm_user";
		String userPassword = "dlwltjxl2@";
        String userPoolName = "psm-user-backup-pool";
        
		StringBuilder sb = new StringBuilder();
		sb.append("adminJjdbcUrl   : ").append("ENC(").append( jasyptEncoding(adminJjdbcUrl) ).append(")<br />")
		  .append("adminUsername   : ").append("ENC(").append( jasyptEncoding(adminUsername) ).append(")<br />")
		  .append("adminPassword   : ").append("ENC(").append( jasyptEncoding(adminPassword) ).append(")<br />")
		  .append("adminPoolName   : ").append("ENC(").append( jasyptEncoding(adminPoolName) ).append(")<br />")
		  .append("===============================================================<br />")
		  .append("userJdbcUrl     : ").append("ENC(").append( jasyptEncoding(userJdbcUrl) ).append(")<br />")
		  .append("userUsername    : ").append("ENC(").append( jasyptEncoding(userUsername) ).append(")<br />")
		  .append("userPassword    : ").append("ENC(").append( jasyptEncoding(userPassword) ).append(")<br />")
		  .append("userPoolName    : ").append("ENC(").append( jasyptEncoding(userPoolName) ).append(")<br />")
		  .append("driverclassname : ").append("ENC(").append( jasyptEncoding(driverclassname) ).append(")<br />");
		
		return sb.toString();
		
	}
	
//	@GetMapping("/jasypt/en")
//	public String enJasypt() {		
//		
////		========= enc =====================
//		objectStorageAttributesVO = new ObjectStorageAttributesVO();
//		String CHARSET_NAME = objectStorageAttributesVO.getCHARSET_NAME();
//	    String HMAC_ALGORITHM = objectStorageAttributesVO.getHMAC_ALGORITHM(); 
//	    String HASH_ALGORITHM = objectStorageAttributesVO.getHASH_ALGORITHM();
//	    String AWS_ALGORITHM = objectStorageAttributesVO.getAWS_ALGORITHM();
//
//	    String SERVICE_NAME = objectStorageAttributesVO.getSERVICE_NAME();
//	    String REQUEST_TYPE = objectStorageAttributesVO.getREQUEST_TYPE();
//
//	    String UNSIGNED_PAYLOAD = objectStorageAttributesVO.getUNSIGNED_PAYLOAD();
//
//	    String REGION_NAME = objectStorageAttributesVO.getREGION_NAME();
//	    String ENDPOINT = objectStorageAttributesVO.getENDPOINT();
//	    String ACCESS_KEY = objectStorageAttributesVO.getACCESS_KEY();
//	    String SECRET_KEY = objectStorageAttributesVO.getSECRET_KEY();
//		
//		
//		StringBuilder sb = new StringBuilder();
//		sb.append("CHARSET_NAME : ").append(jasyptEncoding(CHARSET_NAME)).append("<br />")
//		  .append("HMAC_ALGORITHM : ").append(jasyptEncoding(HMAC_ALGORITHM)).append("<br />")
//		  .append("HASH_ALGORITHM : ").append(jasyptEncoding(HASH_ALGORITHM)).append("<br />")
//		  .append("AWS_ALGORITHM : ").append(jasyptEncoding(AWS_ALGORITHM)).append("<br />")
//		  .append("SERVICE_NAME : ").append(jasyptEncoding(SERVICE_NAME)).append("<br />")
//		  .append("REQUEST_TYPE : ").append(jasyptEncoding(REQUEST_TYPE)).append("<br />")
//		  .append("UNSIGNED_PAYLOAD : ").append(jasyptEncoding(UNSIGNED_PAYLOAD)).append("<br />")
//		  .append("REGION_NAME : ").append(jasyptEncoding(REGION_NAME)).append("<br />")
//		  .append("ENDPOINT : ").append(jasyptEncoding(ENDPOINT)).append("<br />")
//		  .append("ACCESS_KEY : ").append(jasyptEncoding(ACCESS_KEY)).append("<br />")
//		  .append("SECRET_KEY : ").append(jasyptEncoding(SECRET_KEY)).append("<br />");
//		
//		return sb.toString();
//	}
//	
	private String jasyptEncoding(String value) {

		String key = "ez-psm-saas";
		String algorithm = "PBEWITHHMACSHA256ANDAES_256";
		
		StandardPBEStringEncryptor pbeEnc = new StandardPBEStringEncryptor();
		
		pbeEnc.setAlgorithm( algorithm );
		pbeEnc.setPassword(key);
		pbeEnc.setIvGenerator(new RandomIvGenerator());
		pbeEnc.setKeyObtentionIterations( 1000 );		
		pbeEnc.setProviderName( "SunJCE" );				
		pbeEnc.setStringOutputType( "base64" );
		
		return pbeEnc.encrypt(value);
	}
	
}
