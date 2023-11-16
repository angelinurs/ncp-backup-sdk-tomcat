package com.ez.ncpsdktomcat.service;

import java.util.ArrayList;
import java.util.List;

import com.ez.ncpsdktomcat.config.ObjectStorageProps;

/**
 * @author kyoung il pak
 * @version 0.9.0
 * @since 2023.11.12.mon
 * 
 * @apiNote
 * 
 * * method list <br />
 * {@link #getCommand(String, String)<br />
 * 
 */
public class AwsClientService {
	
	
	final private ObjectStorageProps objectStorageProps;
	
	public AwsClientService( ObjectStorageProps objectStorageProps ) {
		this.objectStorageProps = objectStorageProps;
	}

	
	/**
	 * @author kyoung il pak
	 * @implNote get command for sync Buckets
	 * @return sync command
	 */
	public String getCommand( String src, String dest ) {
		
		String endpointSouth = objectStorageProps.getENDPOINT_SOUTH();
		String options = objectStorageProps.getSYNC_BUCKET_COMMAND();
		String awsPath = objectStorageProps.getAWS_PATH();
		
		// --endpoint-url=<objecturl> s3 sync s3://<source> s3://<destination>
		options = options.replace( "<objecturl>", endpointSouth )
		                 .replace( "<source>", src )
		                 .replace( "<destination>", dest );
		
		return awsPath + " " + options;
		
	}
	
	public List<String> getAccessKeyEnvs() {
		List<String> envs = new ArrayList<>();

		
		String access_key = objectStorageProps.getACCESS_KEY();
		String secret_key = objectStorageProps.getSECRET_KEY(); 
		
		envs.add( String.format( "AWS_ACCESS_KEY_ID=%s", access_key ) );
		envs.add( String.format( "AWS_SECRET_ACCESS_KEY=%s", secret_key ) );
		
		return envs;
	}

}
