package com.ez.ncpsdktomcat.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ez.ncpsdktomcat.common.ErrorLogMessage;
import com.ez.ncpsdktomcat.common.FileUtils;
import com.ez.ncpsdktomcat.config.EtcProps;

import lombok.extern.slf4j.Slf4j;
/**
 * @author kyoung il pak
 * @version 0.9.0
 * @since 2023.09.22.fri
 * 
 * @apiNote
 * 
 * * method list <br />
 * {@link https://forums.oracle.com/ords/apexds/post/combining-zip-and-cipher-streams-zipoutputstream-and-cipher-1664 <br />
 * {@link #getEnvs(String) <br />
 * {@link #getCommand(String, String) <br />
 * <br />
 * {@link #RunScript(String[], String[], File, String) <br />
 * {@link #doDump(String, String, String, String, String) <br />
 * 
 * @see LogCollector#getLogs(String) 
 * 
 */
@Slf4j
@Component
public class LogCollector {
	
	@Autowired
	private EtcProps etcProps;
		
	public String[] getLogs( String logPath, String extension ) {
//		String logPath = LOG_PATH + TENENT ;
//		String logPath = LOG_PATH + tenent ;
		
		log.info( "=== Start File List ===" );
		log.info( "= + log Path : {} ", logPath );
		
		Set<String> logFiles;
		String[] arrLogFiles = null;
		try {
			logFiles = FileUtils.listFilesUsingFileWalkAndVisitor( logPath, extension );

			// sort logs
			List<String> logFileList = new ArrayList<>(logFiles);
			Collections.sort(logFileList);
			
			arrLogFiles = logFileList.toArray( new String[logFileList.size()]);
			
			log.info( "==================================" );
			log.info( "return value is ..." );
			log.info( String.join("\n", arrLogFiles));
		} catch (IOException ioe) {
			log.error( ErrorLogMessage.getPrintStackTrace( ioe ) );
			log.error( "can not access to persist volume!!" );
		}
		
		log.info( "=== End File List ===");
		
		return arrLogFiles;		
	}

}
