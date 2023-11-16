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
import java.util.stream.Stream;

import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

	public static boolean getFileExist( TenencySchemaVO vo ) {
		
		String filename = vo.getAbsolutePath();
			       
        File file = new File( filename );
        
        if( ! file.exists() ) {
    		log.info( "{} is not exists. ", filename );
    		return false;
        } else {
        	log.info( "{} is exists. ", filename );
        	return true;
        }
        
	}
	
	public static boolean deleteSchemaFile( TenencySchemaVO vo ) {
		if( getFileExist(vo) ) {
			String filename = vo.getAbsolutePath();
			
			if( filename.length() > 0 ) {
				File file = new File( filename );
				if( file.delete() ) {
					log.info( "{} is deleted.", filename );				
				} else {
					log.info( "{} is not deleted.", filename );
				}	
			}
			
			return true;
		} else {
			return false;
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
//	        	String patternString = "^.*-.*[.]log[.]enc[.]gz$";
	        	String patternString = "^.*-.*[.]log$";
	            if ( !Files.isDirectory(file) && file.getFileName().toString().matches(patternString)) {
	                fileList.add( file.toAbsolutePath().toString() );
	            }
	            return FileVisitResult.CONTINUE;
	        }
	    });
	    return fileList;
	}	

	static public String[] concatenate(String[] first, String[] second)
	{
	    return Stream.of(first, second)
	                    .flatMap(Stream::of)        // 또는 `Arrays::stream`을 사용합니다.
	                    .toArray(String[]::new);
	}
}
