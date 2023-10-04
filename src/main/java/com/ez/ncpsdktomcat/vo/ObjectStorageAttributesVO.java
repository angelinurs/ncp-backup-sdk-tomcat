package com.ez.ncpsdktomcat.vo;

import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * @deprecated will be deprecated
 */
@Getter
@Component
public class ObjectStorageAttributesVO {

    private String CHARSET_NAME;
    private String HMAC_ALGORITHM;
    private String HASH_ALGORITHM;
    private String AWS_ALGORITHM;

    private String SERVICE_NAME;
    private String REQUEST_TYPE;

    private String UNSIGNED_PAYLOAD;

    private String REGION_NAME;
    private String ENDPOINT;
    private String ACCESS_KEY;
    private String SECRET_KEY;
    
    public ObjectStorageAttributesVO( ) {

	    CHARSET_NAME = "UTF-8";
	    HMAC_ALGORITHM = "HmacSHA256";
	    HASH_ALGORITHM = "SHA-256";
	    AWS_ALGORITHM = "AWS4-HMAC-SHA256";

	    SERVICE_NAME = "s3";
	    REQUEST_TYPE = "aws4_request";

	    UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

	    // object storage CSP infomation
	    REGION_NAME = "gov-standard";
	    ENDPOINT = "https://kr.object.gov-ncloudstorage.com";
	    ACCESS_KEY = "LU2uVIWLmbQuI7ngYqnv";
	    SECRET_KEY = "KjYrWWKoybZZXlycnlL8Vm9fCZ3VZqTQfnbSneYn";
		
	}

}
