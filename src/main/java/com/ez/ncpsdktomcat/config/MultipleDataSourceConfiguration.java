package com.ez.ncpsdktomcat.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author kyoung il pak
 * @since 2023.03.27.mon
 * @version 0.9.0
 * @apiNote application.yml 의 설정값 portal 과 user 를 가지고
 *          Multiple Connection 구성
 *          portal 은 primary 구성으로 일반적 사용
 *          user 사용시 Qualifier annotation 명시
 */
@Configuration
public class MultipleDataSourceConfiguration {

    @Primary
    @Bean("portalHikariConfig")
    @ConfigurationProperties(prefix = "spring.portal.datasource.hikari")
    HikariConfig portalHikariConfig() {
        return new HikariConfig();
    }
    
    @Primary
    @Bean("portalDataSource")
    DataSource portalDatasource() throws Exception {
        return new HikariDataSource( portalHikariConfig() );
    }

    @Primary
    @Bean( "portalJdbcTemplate")
    JdbcTemplate adminJdbcTemplate( @Qualifier( "portalDataSource" ) DataSource portalDataSource ) throws Exception {
    	return new JdbcTemplate( portalDatasource() );
    }

    @Bean("userHikariConfig")
    @ConfigurationProperties(prefix = "spring.user.datasource.hikari")
    HikariConfig userHikariConfig() {
        return new HikariConfig();
    }
	
	@Bean( "userDataSource" )
	DataSource userDataSource() throws Exception {
		return new HikariDataSource( userHikariConfig() );
	}
	
	@Bean( "userJdbcTemplate" )
	JdbcTemplate userJdbcTemplate( @Qualifier( "userDataSource" ) DataSource userDataSource ) {
		return new JdbcTemplate(userDataSource);
	}
}
