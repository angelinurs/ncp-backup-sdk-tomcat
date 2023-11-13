package com.ez.ncpsdktomcat.config;

import java.text.SimpleDateFormat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

// ref:// https://www.baeldung.com/spring-boot-yaml-list
@Component
@Getter
@Setter
@ConfigurationProperties( prefix = "application.object-storage" )
public class ObjectStorageProps {

    private String CHARSET_NAME;
    private String HMAC_ALGORITHM;
    private String HASH_ALGORITHM;
    private String AWS_ALGORITHM;

    private String SERVICE_NAME;
    private String REQUEST_TYPE;

    private String UNSIGNED_PAYLOAD;

    private SimpleDateFormat DATE_FORMATTER;
    private SimpleDateFormat TIME_FORMATTER;

    private String REGION_NAME;
    private String ENDPOINT_SEOUL;
    private String ENDPOINT_SOUTH;
    private String ACCESS_KEY;
    private String SECRET_KEY;
    
    private String AWS_PATH;
    private String SYNC_BUCKET_COMMAND;

}
