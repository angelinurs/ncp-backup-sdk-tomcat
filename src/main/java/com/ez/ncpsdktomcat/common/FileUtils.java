package com.ez.ncpsdktomcat.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

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
	
	public static void deleteSchemaFile( String dirName, String schemaName ) {
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
	
	public static void deleteLogFile( String filename ) {
		
		if( filename.length() > 0 ) {
			File file = new File( filename );
			if( file.delete() ) {
				log.info( "{} is deleted.", filename );				
			} else {
				log.info( "{} is not deleted.", filename );
			}
		}
	}
	
	static public Set<String> listFilesUsingFileWalkAndVisitor(String dir, String extension) throws IOException {
	    Set<String> fileList = new HashSet<>();
	    Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
	            if ( !Files.isDirectory(file) && file.getFileName().toString().endsWith( extension ) ) {
	                fileList.add( file.toAbsolutePath().toString() );
	            }
	            return FileVisitResult.CONTINUE;
	        }
	    });
	    return fileList;
	}
}
