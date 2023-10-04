package com.ez.ncpsdktomcat.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

/**
 * @author kyoung il, pak
 * @since 2023.09.19.tue
 * @version 0.9.0
 * @apiNote decoding application properties using jasypt
 *          algorithm: PBEWITHHMACSHA256ANDAES_256
 *          application.yml 에 jasypt.encryptor.bean: jasyptEncryptorAES 추가 해야함. 
 */
@Configuration
@EnableEncryptableProperties
public class JasyptConfigAES {
	
	@Bean( name = "jasyptEncryptorAES" )
	public StringEncryptor stringEncryptor() {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		
		config.setPassword( "ez-psm-saas" );
		config.setAlgorithm( "PBEWITHHMACSHA256ANDAES_256" );
		config.setKeyObtentionIterations( 1000 );
		config.setPoolSize( 1 );
		config.setProviderName( "SunJCE" );
		config.setSaltGeneratorClassName( "org.jasypt.salt.RandomSaltGenerator" );
		config.setIvGeneratorClassName( "org.jasypt.iv.RandomIvGenerator" );
		config.setStringOutputType( "base64" );
		
		encryptor.setConfig( config );
		
		return encryptor;
	}	

}
