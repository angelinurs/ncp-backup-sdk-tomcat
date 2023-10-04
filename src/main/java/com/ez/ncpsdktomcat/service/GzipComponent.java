package com.ez.ncpsdktomcat.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.stereotype.Component;

import com.ez.ncpsdktomcat.common.ErrorLogMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GzipComponent {	
	
	public void decompressGzipFile( String gzipFile, String newFile ) {
		FileInputStream fis = null;
		GZIPInputStream gis = null;
		FileOutputStream fos = null;
		
		byte[] buffer = new byte[1024];
		int len;
		
		try {
			
			log.info( "gzipFile : {}", gzipFile );
			log.info( "newFile : {}", newFile );
			
			
			fis = new FileInputStream( gzipFile );
			gis = new GZIPInputStream( fis );
			fos = new FileOutputStream( newFile );
			
			while( ( len = gis.read( buffer ) ) != -1 ) {
				fos.write( buffer, 0, len );
			}
			
		} catch (Exception e) {
			log.error( ErrorLogMessage.getPrintStackTrace(e) );
		} finally {
			// close resuorce
			if( fos != null ) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error( ErrorLogMessage.getPrintStackTrace(e) );
				}
			}
			if( gis != null ) {
				try {
					gis.close();
				}catch (Exception e) {
					log.error( ErrorLogMessage.getPrintStackTrace(e) );
				}
			}
			if( fis != null ) {
				try {
					fis.close();
				} catch (IOException e) {
					log.error( ErrorLogMessage.getPrintStackTrace(e) );
				}
			}
		}
	}
	
	public void compressGzipFile( String file, String gzipFile ) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		GZIPOutputStream gos = null;
		
		byte[] buffer = new byte[1024];
		int len;
		
		try {

			log.info( "newFile : {}", file );			
			log.info( "gzipFile : {}", gzipFile );
			
			fis = new FileInputStream( file );
			fos = new FileOutputStream( gzipFile );
			gos = new GZIPOutputStream( fos );
			
			while( (len = fis.read(buffer)) != -1 ) {
				gos.write(buffer, 0, len);
			}
			
		} catch (Exception e) {
			log.error( ErrorLogMessage.getPrintStackTrace(e) );
		} finally {
			// close resuorce
			if( gos != null ) {
				try {
					gos.close();
				}catch (Exception e) {
					log.error( ErrorLogMessage.getPrintStackTrace(e) );
				}
			}
			if( fos != null ) {
				try {
					fos.close();
				} catch (IOException e) {
					log.error( ErrorLogMessage.getPrintStackTrace(e) );
				}
			}
			if( fis != null ) {
				try {
					fis.close();
				} catch (IOException e) {
					log.error( ErrorLogMessage.getPrintStackTrace(e) );
				}
			}
		}
	}
}
