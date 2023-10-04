package com.ez.ncpsdktomcat.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import com.ez.ncpsdktomcat.config.ObjectStorageProps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



/**
 * @deprecated will be deprecated 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ObjectStorageService {	
	
	private final ObjectStorageProps objectStorageProps;

	private byte[] sign(String stringData, byte[] key) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        byte[] data = stringData.getBytes( objectStorageProps.getCHARSET_NAME() );
        Mac e = Mac.getInstance( objectStorageProps.getHMAC_ALGORITHM() );        
        e.init(new SecretKeySpec(key, objectStorageProps.getHMAC_ALGORITHM() ));
        
        return e.doFinal(data);
    }

    private String hash(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        MessageDigest e = MessageDigest.getInstance( objectStorageProps.getHASH_ALGORITHM() );
        e.update(text.getBytes( objectStorageProps.getCHARSET_NAME() ));
        
        return Hex.encodeHexString(e.digest());
    }

    private String getStandardizedQueryParameters(String queryString) throws UnsupportedEncodingException {
        TreeMap<String, String> sortedQueryParameters = new TreeMap<>();
        // sort by key name
        if (queryString != null && !queryString.isEmpty()) {
            String[] queryStringTokens = queryString.split("&");
            for (String field : queryStringTokens) {
                String[] fieldTokens = field.split("=");
                if (fieldTokens.length > 0) {
                    if (fieldTokens.length > 1) {
                        sortedQueryParameters.put(fieldTokens[0], fieldTokens[1]);
                    } else {
                        sortedQueryParameters.put(fieldTokens[0], "");
                    }
                }
            }
        }

        StringBuilder standardizedQueryParametersBuilder = new StringBuilder();
        int count = 0;
        for (String key : sortedQueryParameters.keySet()) {
            if (count > 0) {
                standardizedQueryParametersBuilder.append("&");
            }
            standardizedQueryParametersBuilder.append(key).append("=");

            if (sortedQueryParameters.get(key) != null && !sortedQueryParameters.get(key).isEmpty()) {
                standardizedQueryParametersBuilder.append(URLEncoder.encode(sortedQueryParameters.get(key), objectStorageProps.getCHARSET_NAME() ));
            }

            count++;
        }
        return standardizedQueryParametersBuilder.toString();
    }

    public TreeMap<String, String> getSortedHeaders(Header[] headers) {
        TreeMap<String, String> sortedHeaders = new TreeMap<>();
        // sort by header name
        for (Header header : headers) {
            sortedHeaders.put(header.getName(), header.getValue());
        }

        return sortedHeaders;
    }

    public String getSignedHeaders(TreeMap<String, String> sortedHeaders) {
        StringBuilder signedHeadersBuilder = new StringBuilder();
        for (String headerName : sortedHeaders.keySet()) {
            if (signedHeadersBuilder.length() > 0)
                signedHeadersBuilder.append(';');
            signedHeadersBuilder.append(headerName.toLowerCase());
        }
        return signedHeadersBuilder.toString();
    }

    public String getStandardizedHeaders(TreeMap<String, String> sortedHeaders) {
        StringBuilder standardizedHeadersBuilder = new StringBuilder();
        for (String headerName : sortedHeaders.keySet()) {
            standardizedHeadersBuilder.append(headerName.toLowerCase()).append(":").append(sortedHeaders.get(headerName)).append("\n");
        }

        return standardizedHeadersBuilder.toString();
    }

    public String getCanonicalRequest(HttpUriRequest request, String standardizedQueryParameters, String standardizedHeaders, String signedHeaders) {
        StringBuilder canonicalRequestBuilder = new StringBuilder().append(request.getMethod()).append("\n")
            .append(request.getURI().getPath()).append("\n")
            .append(standardizedQueryParameters).append("\n")
            .append(standardizedHeaders).append("\n")
            .append(signedHeaders).append("\n")
            .append(  objectStorageProps.getUNSIGNED_PAYLOAD() );

        return canonicalRequestBuilder.toString();
    }

    public String getScope(String datestamp, String regionName) {
        StringBuilder scopeBuilder = new StringBuilder().append(datestamp).append("/")
            .append(regionName).append("/")
	        .append( objectStorageProps.getSERVICE_NAME() ).append("/")
	        .append( objectStorageProps.getREQUEST_TYPE() );
        return scopeBuilder.toString();
    }

    public String getStringToSign(String timestamp, String scope, String canonicalRequest) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		StringBuilder stringToSignBuilder = new StringBuilder( objectStorageProps.getAWS_ALGORITHM() )
            .append("\n")
            .append(timestamp).append("\n")
            .append(scope).append("\n")
            .append(hash(canonicalRequest));

        return stringToSignBuilder.toString();
    }

    public String getSignature(String secretKey, String datestamp, String regionName, String stringToSign) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        byte[] kSecret = ("AWS4" + secretKey).getBytes( objectStorageProps.getCHARSET_NAME() );
        byte[] kDate = sign(datestamp, kSecret);
        byte[] kRegion = sign(regionName, kDate);
        byte[] kService = sign( objectStorageProps.getSERVICE_NAME(), kRegion);
        byte[] signingKey = sign( objectStorageProps.getREQUEST_TYPE(), kService);

        return Hex.encodeHexString(sign(stringToSign, signingKey));
    }

    public String getAuthorization(String accessKey, String scope, String signedHeaders, String signature) {
        String signingCredentials = accessKey + "/" + scope;
        String credential = "Credential=" + signingCredentials;
        String signerHeaders = "SignedHeaders=" + signedHeaders;
        String signatureHeader = "Signature=" + signature;

        StringBuilder authHeaderBuilder = new StringBuilder().append( objectStorageProps.getAWS_ALGORITHM() ).append(" ")
												            .append(credential).append(", ")
												            .append(signerHeaders).append(", ")
												            .append(signatureHeader);

        return authHeaderBuilder.toString();
    }

    public void authorization(HttpUriRequest request, String regionName, String accessKey, String secretKey) throws Exception {
        Date now = new Date();
        SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyyMMdd\'T\'HHmmss\'Z\'");
        DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        String datestamp = DATE_FORMATTER.format(now);
        String timestamp = TIME_FORMATTER.format(now);

        request.addHeader("X-Amz-Date", timestamp);

        request.addHeader("X-Amz-Content-Sha256",  objectStorageProps.getUNSIGNED_PAYLOAD() );

        String standardizedQueryParameters = getStandardizedQueryParameters(request.getURI().getQuery());

        TreeMap<String, String> sortedHeaders = getSortedHeaders(request.getAllHeaders());
        String signedHeaders = getSignedHeaders(sortedHeaders);
        String standardizedHeaders = getStandardizedHeaders(sortedHeaders);

        String canonicalRequest = getCanonicalRequest(request, standardizedQueryParameters, standardizedHeaders, signedHeaders);
        log.info("> canonicalRequest :");
        log.info(canonicalRequest);

        String scope = getScope(datestamp, regionName);

        String stringToSign = getStringToSign(timestamp, scope, canonicalRequest);
        log.info("> stringToSign :");
        log.info(stringToSign);

        String signature = getSignature(secretKey, datestamp, regionName, stringToSign);

        String authorization = getAuthorization(accessKey, scope, signedHeaders, signature);
        request.addHeader("Authorization", authorization);
    }

    public void putObject(String bucketName, String objectName, String localFilePath) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPut request = new HttpPut( objectStorageProps.getENDPOINT() + "/" + bucketName + "/" + objectName);
        request.addHeader("Host", request.getURI().getHost());
        request.setEntity(new FileEntity(new File(localFilePath)));

        authorization(request, objectStorageProps.getREGION_NAME(), objectStorageProps.getACCESS_KEY(), objectStorageProps.getSECRET_KEY() );

        HttpResponse response = httpClient.execute(request);
        log.info("Response : " + response.getStatusLine());
    }
    
    public void putObjectByByteArrayEntity(String bucketName, String objectName, byte[] bytes ) throws Exception {
    	HttpClient httpClient = HttpClientBuilder.create().build();
    	
    	HttpPut request = new HttpPut( objectStorageProps.getENDPOINT() + "/" + bucketName + "/" + objectName);
    	request.addHeader("Host", request.getURI().getHost());
    	request.setEntity( new ByteArrayEntity( bytes ) );
    	
    	
    	authorization(request, objectStorageProps.getREGION_NAME(), objectStorageProps.getACCESS_KEY(), objectStorageProps.getSECRET_KEY() );
    	
    	HttpResponse response = httpClient.execute(request);
    	log.info("Response : " + response.getStatusLine());
    }

    public void getObject(String bucketName, String objectName, String localFilePath) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet( objectStorageProps.getENDPOINT() + "/" + bucketName + "/" + objectName);
        request.addHeader("Host", request.getURI().getHost());

        authorization(request,  objectStorageProps.getREGION_NAME(),  objectStorageProps.getACCESS_KEY(),  objectStorageProps.getSECRET_KEY() );

        HttpResponse response = httpClient.execute(request);
        log.info("Response : " + response.getStatusLine());

        InputStream is = response.getEntity().getContent();
        File targetFile = new File(localFilePath);
        OutputStream os = new FileOutputStream(targetFile);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();
    }

    public void listObjects(String bucketName, String queryString) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        
        URI uri = new URI( objectStorageProps.getENDPOINT() + "/" + bucketName + "?" + queryString);
        log.info( objectStorageProps.getENDPOINT() + "/" + bucketName + "?" + queryString );
        
        HttpGet request = new HttpGet(uri);
        request.addHeader("Host", request.getURI().getHost());

        authorization(request, objectStorageProps.getREGION_NAME(), objectStorageProps.getACCESS_KEY(),  objectStorageProps.getSECRET_KEY() );

        HttpResponse response = httpClient.execute(request);
        log.info("> Response : " + response.getStatusLine());
        int i;
        InputStream is = response.getEntity().getContent();
        StringBuffer buffer = new StringBuffer();
        byte[] b = new byte[4096];
        while ((i = is.read(b)) != -1) {
            buffer.append(new String(b, 0, i));
        }
        
        log.info(buffer.toString());

    }

    public void TestAction( String bucketName, String objectName, String sourceFilePath, String targetFilePath ) throws Exception {
//        bucketName = "sample-bucket";
//        objectName = "sample-object.txt";
//        sourceFilePath = "/tmp/source.txt";
//        targetFilePath = "/tmp/target.txt";

//        putObject(bucketName, objectName, sourceFilePath);
//
//        getObject(bucketName, objectName, targetFilePath);

        String queryString = "max-keys=10&delimiter=/";
        listObjects(bucketName, queryString);
    }
}