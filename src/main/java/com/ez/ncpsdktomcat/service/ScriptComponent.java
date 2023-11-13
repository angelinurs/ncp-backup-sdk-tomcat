package com.ez.ncpsdktomcat.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ez.ncpsdktomcat.common.ErrorLogMessage;
import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kyoung il pak
 * @version 0.9.0
 * @since 2023.09.22.fri
 * 
 * @apiNote
 * 
 * * method list <br />
 * {@link #getLogEnvs(String, String) <br />
 * {@link #getSchemaEnvs(String, TenencySchemaVO) <br />
 * <br />
 * {@link #getCommand(String) <br />
 * <br />
 * {@link #RunScript(String[], String[], File, String) <br />
 * <br />
 * {@link #doDumpSchemas(TenencySchemaVO, String) <br />
 * {@link #doDumpLogs(String, String) <br />
 * 
 */
@Slf4j
@Getter
@Setter
@Component
public class ScriptComponent implements EnvironmentAware {
	
	// etc
	private String dirPath;
	private String schemaScript;
	private String logScript;

	// user properties
	private String userUrl;	
	private String userUser;	
	private String userPassword;	
	private String userHost; 
	private String userPort;
	private String userDbname;
	
	// portal properties
	private String portalUrl;	
	private String portalUser;	
	private String portalPassword;	
	private String portalHost; 
	private String portalPort;
	private String portalDbname;
	
	private String shellScriptPath;
	
	@Override
	public void setEnvironment(Environment environment) {			
		
		// set user properties
		this.dirPath = environment.getProperty("application.etc.DIR_PATH");
		this.schemaScript = environment.getProperty("application.etc.SCHEMA_SCRIPT");
		this.logScript = environment.getProperty("application.etc.LOG_SCRIPT");
		
		// set user properties  
		this.userUrl = environment.getProperty("spring.user.datasource.hikari.jdbc-url");
		this.userUser = environment.getProperty("spring.user.datasource.hikari.username");
		this.userPassword = environment.getProperty("spring.user.datasource.hikari.password");
		
		if( userUrl != null ) {
			String[] tokens = userUrl.split("/");
			userHost = tokens[ tokens.length -2 ].split(":")[0];
			userPort = tokens[ tokens.length -2 ].split(":")[1];
			// Dangling meta character '\\'
			userDbname = tokens[ tokens.length -1 ].split("\\?")[0];
		} else {
			log.error( "[ cloud config Error ] Can not to take remote `application.yml` file" );
		}
		
		// set portal properties
		this.portalUrl = environment.getProperty("spring.portal.datasource.hikari.jdbc-url");
		this.portalUser = environment.getProperty("spring.portal.datasource.hikari.username");
		this.portalPassword = environment.getProperty("spring.portal.datasource.hikari.password");
		
		if( portalUrl != null ) {
			String[] tokens = portalUrl.split("/");
			portalHost = tokens[ tokens.length -2 ].split(":")[0];
			portalPort = tokens[ tokens.length -2 ].split(":")[1];
			// Dangling meta character '\\'
			portalDbname = tokens[ tokens.length -1 ].split("\\?")[0];
		} else {
			log.error( "[ cloud config Error ] Can not to take remote `application.yml` file" );
		}
	}	

	/**
	 * @author kyoung il pak
	 * @implNote get scheme run script environments
	 * @return get environment list
	 */
	private List<String> getSchemaEnvs( String kind, TenencySchemaVO vo) {
		
		List<String> envs = new ArrayList<>();
		
		switch( kind ) {
		case "portal" : 
			// process.runtime.exec param portal envArray
			envs.add( String.format( "PGPASSWORD=%s", this.getPortalPassword() ) );
			envs.add( String.format( "PGHOST=%s", this.getPortalHost() ) );
			envs.add( String.format( "PGPORT=%s", this.getPortalPort() ) );
			envs.add( String.format( "PGDATABASE=%s", this.getPortalDbname() ) );
			envs.add( String.format( "PGUSER=%s", this.getPortalUser() ) );
//			envs.add( String.format( "SCHEMA=%s", "psm_sc_svc171" ) );
			break;
		case "user" : 
			// process.runtime.exec param user envArray
			envs.add( String.format( "PGPASSWORD=%s", this.getUserPassword() ) );
			envs.add( String.format( "PGHOST=%s", this.getUserHost() ) );
			envs.add( String.format( "PGPORT=%s", this.getUserPort() ) );
			envs.add( String.format( "PGDATABASE=%s", this.getUserDbname() ) );
			envs.add( String.format( "PGUSER=%s", this.getUserUser() ) );
//			envs.add( String.format( "SCHEMA=%s", "psm_sc_svc171" ) );
			break;
		}
		
		envs.add( String.format( "FILE_NAME=%s", vo.getAbsolutePath() ) );
		envs.add( String.format( "SCHEMA=%s", vo.getSchema() ) );
		envs.add( String.format( "KEY=%s", vo.getKey() ) );
		
		return envs;
	}

	/**
	 * @author kyoung il pak
	 * @implNote get log run script environments
	 * @return get environment list
	 */
	private List<String> getLogEnvs( String filename, String key ) {
		
		List<String> envs = new ArrayList<>();
		
		// process.runtime.exec param portal envArray
		envs.add( String.format( "FILE_NAME=%s", filename ) );
		envs.add( String.format( "KEY=%s", key ) );
		
		return envs;
	}

	/**
	 * @author kyoung il pak
	 * @implNote get run command
	 * @return get command list
	 */
	private List<String> getCommand( String scriptName ) {
		
		List<String> cmd = new ArrayList<>();
		
		String script = this.dirPath + "/" + scriptName; 
		cmd.add( script );
		
		return cmd;
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote run script
	 */
	public void RunScript ( String[] envs, String[] cmd, File dir, String workName ) {
		//
		log.info( "Init to {} !!", workName );
		
		StringBuilder stdoutLog = new StringBuilder();
		StringBuilder stderrLog = new StringBuilder();
		
		Process process = null;
		BufferedReader br = null;
		
		try {
			process = Runtime.getRuntime().exec( cmd, envs, dir );
			
			// log stdout
			br = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
			
			String line = null;
			
			while( (line = br.readLine()) != null ) {
				stdoutLog.append(line).append("\n");
			}
			
			// log stderr
			br = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );
			
			line = null;
			
			while( (line = br.readLine()) != null ) {
				stderrLog.append(line).append("\n");
			}
			
			br.close();
			
			if( stderrLog.length() > 0 ) {
				log.error( stderrLog.toString() );
			}
			
			log.info( "Success to {} !!", workName );
			
		} catch (IOException e) {
			log.error( ErrorLogMessage.getPrintStackTrace( e ) );	
		} finally {
			if( br != null )
				try {
					br.close();
				} catch (IOException e) {
					log.error( ErrorLogMessage.getPrintStackTrace( e ) );	
				}
		}
	}

	/**
	 * @author kyoung il pak
	 * @implNote dump DB schemas
	 * @return final run commands 
	 */
	public String doDumpSchemas( TenencySchemaVO vo, String kind ) {
		
		List<String> envp = this.getSchemaEnvs(kind, vo );
		
		List<String> cmd = this.getCommand( this.schemaScript );
		
//		log.error( " cmd : {}", String.join( " ", cmd ) );
//		
//		log.error( "{}", String.join(" ", cmd.toArray( new String[ cmd.size() ] ) ) );
		
		this.RunScript( envp.toArray( new String[ envp.size() ]), 
				cmd.toArray( new String[ cmd.size() ]),
				new File( dirPath ),
				vo.getSchema()
				); 
		
		return String.join( " ", cmd.toArray( new String[ cmd.size() ]) ); 
	}
		
	/**
	 * @author kyoung il pak
	 * @implNote dump logs
	 * @return final run commands 
	 */ 
	public String doDumpLogs( String filename, String key ) {
		
		List<String> envp = this.getLogEnvs( filename, key );
		
		List<String> cmd = this.getCommand( this.logScript );
		
//		log.error( " cmd : {}", String.join( " ", cmd ) );
//		
//		log.error( "cmd to array {}", String.join(" ", cmd.toArray( new String[ cmd.size() ] ) ) );
//		
//		log.error( "envp to array {}", String.join(" ", envp.toArray( new String[ envp.size() ] ) ) );
		
		this.RunScript( envp.toArray( new String[ envp.size() ]), 
				cmd.toArray( new String[ cmd.size() ]),
				new File( dirPath ),
				dirPath
				);
		
		return String.join( " ", cmd.toArray( new String[ cmd.size() ]) ); 
	}
}
