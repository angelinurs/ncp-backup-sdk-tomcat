package com.ez.ncpsdktomcat.controller;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ez.ncpsdktomcat.service.BackupComponent;
import com.ez.ncpsdktomcat.service.DBService;
import com.ez.ncpsdktomcat.service.FileEncrypterDecrypter;
import com.ez.ncpsdktomcat.service.GzipComponent;
import com.ez.ncpsdktomcat.service.PsqlClientComponent;
import com.ez.ncpsdktomcat.vo.TenencySchemaVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/db")
public class DBController {
		
	@Autowired
	@Qualifier( value = "userJdbcTemplate" )
	private JdbcTemplate jdbcTemplate;
	
	private DBService dbService;
	
	@Autowired
	private PsqlClientComponent psqlClientComponent;
	
	@Autowired
	private BackupComponent backupComponent;
	
	private FileEncrypterDecrypter fileEncrypterDecrypter;
	
	@Autowired
	private GzipComponent gzipComponent;
		
	@GetMapping("/list2")
	public String getSchemaList2() {
		
		dbService = new DBService( jdbcTemplate );
		
		List<TenencySchemaVO> results =  dbService.getSchemaList();
		
		StringBuilder sb = new StringBuilder();
		for( TenencySchemaVO vo : results ) {
			sb.append( vo.toString() ).append( "<br />" );
			
		}
		
		log.error( "result index 0 : {}", results.get(0) );
		
		return sb.toString();
		
	}
	
	@GetMapping("/dump")
	public String doDump() {
		String key = "naru";
		String schema = "psm_sc_svc171";	
		String date = "$(date +%Y)-$(date +%m)-$(date +%d)";
		String time = "$$(date +%H):$(date +%M):$(date +%S)";
		
		String command = psqlClientComponent.doDump(schema, date, time, "user", key );
		
		return command; 
		
	}
	
	@GetMapping("dumpall")
	public String dumpall() {
		String key = "naru";
		
		dbService = new DBService( jdbcTemplate );
		
		// inquiry schema list
		List<TenencySchemaVO> results =  dbService.getSchemaList();
		
		log.info("============ Start Backup ================");
		StringBuilder sb = new StringBuilder();
		for( TenencySchemaVO vo : results ) {
			sb.append( vo.toString() ).append( "<br />" );
		}
		
		log.info( "result index 0 : {}", results.get(0) );

		for( TenencySchemaVO vo : results ) {
			
			log.info("\n# # == Start ==" );
			
			Instant startTime = Instant.now();
			
			String time = vo.getTime().split( "[.]" )[0];
			
			String command = psqlClientComponent.doDump( vo.getSchema(), vo.getDate(), time, "user", key );
			
			Instant endTime = Instant.now();
			
			long diffTime = Duration.between(startTime, endTime).toMillis();
			
			StringBuilder sbStatus = new StringBuilder();
			sbStatus.append( String.format( "======= %s - work table =======\n", vo.getSchema() ) )
			  		.append( String.format( "Start Time    : %s\n", startTime) )
			  		.append( String.format( "End   Time    : %s\n", endTime) )
			  		.append( String.format( "Duration      : %s\n", diffTime) )
//					.append( String.format( "shell command : %s\n", command) )
					.append( "" );
			
			log.info("# # == End ==\n" );
			
		}
		log.info("============= End Backup ================");
		
		return sb.toString();
		
	}
	
	@GetMapping("/dumpall_2")
	public String dumpall_2() {
		List<TenencySchemaVO> users = backupComponent.dumpall( "user" );
		List<TenencySchemaVO> portals = backupComponent.dumpall( "portal" );
		
		backupComponent.exportToObjectStorage( users );
		backupComponent.exportToObjectStorage( portals );
		
		return "done";
	}
	
	@GetMapping( "/compress" )
	public String doCompress() {
		String dirPath = "/home/naru/temp";
		String schema = "psm_sc_svc171";	
		
		String filename = String.format( "%s/%s.tar", dirPath, schema );
		String gzipFilename = String.format( "%s/%s.tar.gz", dirPath, schema );
		gzipComponent.compressGzipFile( filename, gzipFilename );
		
		return gzipFilename;
	}
	
	@GetMapping( "/ecrypt" ) 
	public String doEncrypt() {
		
		String filename = "/home/naru/temp/baz.enc";
		String originalContent = "foobar";
		String decryptedContent = null;
		
	    SecretKey secretKey = null;
	    
	    try {
			secretKey = KeyGenerator.getInstance("AES").generateKey();
			fileEncrypterDecrypter = new FileEncrypterDecrypter(secretKey, "AES/CBC/PKCS5Padding");
			fileEncrypterDecrypter.encrypt(originalContent, filename );
			
			decryptedContent = fileEncrypterDecrypter.decrypt( filename );
//	    	assertThat(decryptedContent, is(originalContent));
			
//			new File("baz.enc").delete(); // cleanup
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} 

	    return String.format("originalContent : %s, decryptedContent : %s", originalContent, decryptedContent );
	}
	
}
