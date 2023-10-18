package com.ez.ncpsdktomcat.vo;

import java.io.File;
import java.util.Arrays;

import lombok.Getter;

@Getter
public class LogMaterialVO {
	private String key;
	
	private String sourcePath;
	private String tenentName;
	private String fileName;
	private String objectPath;
	private String fianlFileName;
	private String finalSourcePath;
	private boolean valid;
	
	public LogMaterialVO( String sourcePath, String key ) {
		this.sourcePath = sourcePath;
		this.key = key;
		
		String[] items = sourcePath.split( "[/]" );
		
		this.valid = items[3].equals( "schema" )? true: false;
		
		if( valid ) {
			this.tenentName = items[ 4 ];
			if( this.tenentName.startsWith( "svc" ) ) {
				this.tenentName = String.format( "psm-sc-%s", this.tenentName );
			}
			
			this.tenentName = items[ 4 ].startsWith( "svc" )? String.format( "psm_sc_%s", this.tenentName ): items[ 4 ];
			
			this.fileName = items[ items.length-1 ];
			this.fianlFileName = this.fileName + ".enc.gz";
			this.finalSourcePath = String.join( File.separator, Arrays.copyOfRange( items, 0, items.length-1 ) ) + File.separator + this.fianlFileName;
			
			String[] objectPathArray = Arrays.copyOfRange( items, 5, items.length-1 );
			
//			this.objectPath = "/logs/" + String.join( "/", objectPathArray );
			this.objectPath = String.join( File.separator, objectPathArray ) + File.separator;
			
		}
		
	}
}
