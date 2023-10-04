package com.ez.ncpsdktomcat.common;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

	public static String getFileList( String dirName, String schemaName ) {
	       
        File dir = new File(dirName);
        File files[] = dir.listFiles();
        
        String targetFilename = "";
        
        for( File file : files ) {
        	
        	if( file.isFile() && file.getName().startsWith(schemaName) ) {
        		targetFilename  = file.getName();
        		log.info( "{} is exists. ", targetFilename );
        		break;
        	} else {
        		log.info( "{} is not exists. ", targetFilename );
        		
        	}
        }
        
		return targetFilename;
	}
	
	public static void deleteFile( String dirName, String schemaName ) {
		String filename = getFileList(dirName, schemaName);
		
		if( filename.length() > 0 ) {
			File file = new File( dirName + filename );
			if( file.delete() ) {
				log.info( "{} is deleted.", filename );				
			} else {
				log.info( "{} is not deleted.", filename );
			}
		}
	}
}
