package com.ez.ncpsdktomcat.vo;

import java.io.File;
import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogMaterialVO {
	private String key;
	
	private String sourcePath;
	private String tenentName;
	private String profile;
	private String fileName;
	private String objectPath;
	private String fianlFileName;
	private String finalSourcePath;
	private boolean valid;
	
	public LogMaterialVO( String sourcePath ) {
		this.sourcePath = sourcePath;
		
		String[] items = sourcePath.split( "[/]" );
		
		this.valid = items[3].equals( "schema" )? true: false;
		
		if( valid ) {
//			this.tenentName = items[ 4 ];
//			if( this.tenentName.startsWith( "svc" ) ) {
//				this.tenentName = String.format( "psm-sc-%s", this.tenentName );
//			}
			
			this.profile = items[ 4 ];
			this.tenentName = this.profile.startsWith( "svc" )? String.format( "psm-sc-%s", this.profile ): this.profile;
			
			this.fileName = items[ items.length-1 ];
			this.fianlFileName = this.fileName + ".enc.gz";
			this.finalSourcePath = String.join( File.separator, Arrays.copyOfRange( items, 0, items.length-1 ) ) + File.separator + this.fianlFileName;
			
			String[] objectPathArray = Arrays.copyOfRange( items, 5, items.length-1 );
			
//			this.objectPath = "/logs/" + String.join( "/", objectPathArray );
			this.objectPath = String.join( File.separator, objectPathArray ) + File.separator;
			
		}
		
	}
}
