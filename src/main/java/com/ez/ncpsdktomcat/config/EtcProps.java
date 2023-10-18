package com.ez.ncpsdktomcat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

// ref:// https://www.baeldung.com/spring-boot-yaml-list
@Component
@Getter
@Setter
@ConfigurationProperties( prefix = "application.etc" )
public class EtcProps {

    private String DIR_PATH;
    private String LOG_PATH;
    private String LOG_SCRIPT;
    private String SCHEMA_SCRIPT;
    private String SCHEDULE_TIME;

}
