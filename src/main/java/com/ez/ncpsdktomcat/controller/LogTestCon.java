package com.ez.ncpsdktomcat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ez.ncpsdktomcat.deprecated.GzipComponent;
import com.ez.ncpsdktomcat.service.LogCollector;

@RestController
@RequestMapping( "/log" )
public class LogTestCon {
	
	@Autowired
	private LogCollector logCollector;
	
	@Autowired
	private GzipComponent gzipComponent;
	
	// 1. get log list
	// 2. to compress and encoding with openssl each other
	// 3. store to specific object buckets
	
	@GetMapping( "/list" )
	public String[] getLogList() {
		
		return logCollector.getLogs( "/ez-sys", ".log" );
	}
	
	@GetMapping( "/compress" )
	public void toCompress() {
		String[] logs = null;
		logs = logCollector.getLogs( "/ez-sys", ".log" );
		
		if( logs != null ) {
			for( String log : logs ) {
				gzipComponent.compressGzipFile( log, log+"gz" );
				
			}
		}
	}
	
}
