package com.ez.ncpsdktomcat.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultipartUpload;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.ez.ncpsdktomcat.common.ErrorLogMessage;
import com.ez.ncpsdktomcat.config.ObjectStorageProps;

import lombok.extern.slf4j.Slf4j;

/**
 * @author kyoung il pak
 * @version 0.9.0
 * @since 2023.09.25.mon
 * 
 * @apiNote
 * 
 * * method list <br />
 * {@link #getBucketList(String)} <br />
 * {@link #getObjectList(String)} <br />
 * <br />
 * {@link #createBucket(String)} <br />
 * {@link #deleteBucket(String)} <br />
 * <br />
 * {@link #uploadObject(String, String, String, String)} <br />
 * {@link #uploadMultipart(String, String)} <br />
 * <br />
 * {@link #downloadObject(String, String, String)} <br />
 * <br />
 * {@link #deleteObject(String, String)} <br />
 * 
 * @see <a href="https://guide-gov.ncloud-docs.com/docs/storage-storage-8-1">gov-ncloud SDK Docs</a>
 * @see <a href="https://github.com/aws/aws-sdk-java/tree/master/aws-java-sdk-s3">AWS Java SDK GitHub</a>
 * 
 */
@Slf4j
public class ObjectStorageS3 {
	
	// S3 client
	private final AmazonS3 s3;
	
	public ObjectStorageS3( ObjectStorageProps objectStorageProps ) {

		String endPoint = objectStorageProps.getENDPOINT();
		String regionName = objectStorageProps.getREGION_NAME();
		String accessKey = objectStorageProps.getACCESS_KEY();
		String secretKey = objectStorageProps.getSECRET_KEY();
		
		// S3 client
		this.s3 = AmazonS3ClientBuilder.standard()
			    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
			    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
			    .build();
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote get Bucket List from Object Storage
	 * @return List of Bucket
	 */
	public List<Bucket> getBucketList() {
				
		List<Bucket> buckets = null;

		try {
		    buckets = s3.listBuckets();
		    System.out.println("Bucket List: ");
		    for (Bucket bucket : buckets) {
//		        System.out.println("    name=" + bucket.getName() + ", creation_date=" + bucket.getCreationDate() + ", owner=" + bucket.getOwner().getId());
		        log.info("    name=" + bucket.getName() + ", creation_date=" + bucket.getCreationDate() + ", owner=" + bucket.getOwner().getId());
		    }
		    
		} catch (AmazonS3Exception e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		}
		
		return buckets;
	}

	/**
	 * @author kyoung il pak
	 * @implNote get Object List from specific bucket
	 * @param bucketName
	 * @return List of Object
	 */
	public ObjectListing getObjectList( String bucketName ) {	
		
		ObjectListing objectListing = null;

		// list all in the bucket
		try {
		    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
		        .withBucketName(bucketName)
		        .withMaxKeys(300);

		    objectListing = s3.listObjects(listObjectsRequest);

		    System.out.println("Object List:");
		    while (true) {
		        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
		            System.out.println("    name=" + objectSummary.getKey() + ", size=" + objectSummary.getSize() + ", owner=" + objectSummary.getOwner().getId());
		        }

		        if (objectListing.isTruncated()) {
		            objectListing = s3.listNextBatchOfObjects(objectListing);
		        } else {
		            break;
		        }
		    }
		} catch (AmazonS3Exception e) {
		    log.error(e.getErrorMessage());
		}

		// top level folders and files in the bucket
		try {
		    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
		        .withBucketName(bucketName)
		        .withDelimiter("/")
		        .withMaxKeys(300);

		    objectListing = s3.listObjects(listObjectsRequest);

		    log.info("Folder List:");
		    for (String commonPrefixes : objectListing.getCommonPrefixes()) {
		    	log.info("    name=" + commonPrefixes);
		    }

		    log.info("File List:");
		    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
		    	log.info("    name=" + objectSummary.getKey() + ", size=" + objectSummary.getSize() + ", owner=" + objectSummary.getOwner().getId());
		    }
		} catch (AmazonS3Exception e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		}
		
		return objectListing;
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote create Bucket
	 * @param bucketName
	 * @return status flag
	 */
	public boolean createBucket( String bucketName ) {
		boolean flag = false; 
		try {
		    // create bucket if the bucket name does not exist
		    if (s3.doesBucketExistV2(bucketName)) {
		    	flag = false;
		        log.info("Bucket {} already exists.", bucketName);
		    } else {
		    	flag = true;
		        s3.createBucket(bucketName);
		        log.info("Bucket {} has been created.", bucketName);
		    }
		} catch (AmazonS3Exception e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		}
		
		return flag;
				
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote delete Bucket  
	 * 			 1. check bucket 
	 *           2. truncated list of MultipartUploads
	 *           3. delete object
	 * 
	 * @param bucketName
	 * @return none
	 */
	public void deleteBucket( String bucketName ) {
				
		try {
		    // delete bucket if the bucket exists
		    if (s3.doesBucketExistV2(bucketName)) {
		        // delete all objects
		        ObjectListing objectListing = s3.listObjects(bucketName);
		        while (true) {
		            for (Iterator<?> iterator = objectListing.getObjectSummaries().iterator(); iterator.hasNext();) {
		                S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
		                s3.deleteObject(bucketName, summary.getKey());
		            }

		            if (objectListing.isTruncated()) {
		                objectListing = s3.listNextBatchOfObjects(objectListing);
		            } else {
		                break;
		            }
		        }

		        // abort incomplete multipart uploads
		        MultipartUploadListing multipartUploadListing = s3.listMultipartUploads(new ListMultipartUploadsRequest(bucketName));
		        while (true) {
		            for (Iterator<?> iterator = multipartUploadListing.getMultipartUploads().iterator(); iterator.hasNext();) {
		                MultipartUpload multipartUpload = (MultipartUpload)iterator.next();
		                s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, multipartUpload.getKey(), multipartUpload.getUploadId()));
		            }

		            if (multipartUploadListing.isTruncated()) {
		                ListMultipartUploadsRequest listMultipartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
		                listMultipartUploadsRequest.withUploadIdMarker(multipartUploadListing.getNextUploadIdMarker());
		                multipartUploadListing = s3.listMultipartUploads(listMultipartUploadsRequest);
		            } else {
		                break;
		            }
		        }

		        s3.deleteBucket(bucketName);
		        log.info("Bucket {} has been deleted.", bucketName);
		    } else {
		        log.info("Bucket {} does not exist.", bucketName);
		    }
		} catch (AmazonS3Exception e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		}		
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote upload Object
	 * 
	 * @param bucketName  - specific bucket Name
	 * @param schemaName  - specific schema Name
	 * @param folderName  - create object folder
	 * @param objectName  - destination file name
	 * @param filePath    - source file path
	 *  
	 * @return none
	 */
	public boolean uploadObject( String bucketName, String folerName, String objectName, String filePath ) {

		boolean status = false;
		// create folder
//		String folderName = "sample-folder/";

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(0L);
		objectMetadata.setContentType("application/x-directory");
		PutObjectRequest putObjectRequest = new PutObjectRequest( bucketName, folerName, new ByteArrayInputStream(new byte[0]), objectMetadata);

		try {
		    s3.putObject(putObjectRequest);
		    log.info( "Folder {} has been created.", folerName );
		} catch (AmazonS3Exception e) {
		    log.error( "{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
		    log.error( "{}", ErrorLogMessage.getPrintStackTrace(e) );
		}

		// upload local file
		// ex.
//		String objectName = "sample-object";
//		String filePath = "/tmp/sample.txt";

		try {
		    s3.putObject( bucketName, objectName, new File(filePath) );
		    log.error( "Object {} has been created.", objectName );
		} catch ( AmazonS3Exception e ) {
		    log.error( "{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch( SdkClientException e ) {
		    log.error( "{}", ErrorLogMessage.getPrintStackTrace(e) );
		}
		
		return status;
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote upload multipart
	 * 
	 * @param bucketName
	 * @param objectName
	 *  
	 * @return none
	 */
	public CompleteMultipartUploadResult uploadMultipart( String bucketName, String objectName ) {
		String filename = String.format("%s", objectName );
		File file = new File(filename);
		long contentLength = file.length();
		long partSize = 10 * 1024 * 1024;
		
		CompleteMultipartUploadResult completeMultipartUploadResult = null;

		try {
		    // initialize and get upload ID
		    InitiateMultipartUploadResult initiateMultipartUploadResult = s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, objectName));
		    String uploadId = initiateMultipartUploadResult.getUploadId();

		    // upload parts
		    List<PartETag> partETagList = new ArrayList<PartETag>();

		    long fileOffset = 0;
		    for (int i = 1; fileOffset < contentLength; i++) {
		        partSize = Math.min(partSize, (contentLength - fileOffset));

		        UploadPartRequest uploadPartRequest = new UploadPartRequest()
		            .withBucketName(bucketName)
		            .withKey(objectName)
		            .withUploadId(uploadId)
		            .withPartNumber(i)
		            .withFile(file)
		            .withFileOffset(fileOffset)
		            .withPartSize(partSize);

		        UploadPartResult uploadPartResult = s3.uploadPart(uploadPartRequest);
		        partETagList.add(uploadPartResult.getPartETag());

		        fileOffset += partSize;
		    }

		    // abort
		    // s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, objectName, uploadId));

		    // complete
		    completeMultipartUploadResult = s3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETagList));
		} catch (AmazonS3Exception e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		}
		
		return completeMultipartUploadResult;
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote download object
	 * 
	 * @param bucketName
	 * @param objectName
	 * @param downloadPath
	 *  
	 *  @return {@link boolean} flag false normal, true not-normal
	 */
	public boolean downloadObject( String bucketName, String objectName, String downloadPath ) {

		boolean flag = false;
				
		S3ObjectInputStream s3ObjectInputStream = null;
		OutputStream outputStream = null;
		// ex
//		String bucketName = "sample-bucket";
//		String objectName = "sample-object";
//		String downloadPath = "/tmp/sample-object";
		
		// download object
		try {
		    if( !s3.doesObjectExist(bucketName, objectName) ) {
		    	flag = false;
		    	
		    } else {
		    	flag = true;
		    	
		    	S3Object s3Object = s3.getObject(bucketName, objectName);
		    	
		    	s3ObjectInputStream = s3Object.getObjectContent();
		    	
		    	outputStream = new BufferedOutputStream(new FileOutputStream(downloadPath));
		    	byte[] bytesArray = new byte[4096];
		    	int bytesRead = -1;
		    	while ((bytesRead = s3ObjectInputStream.read(bytesArray)) != -1) {
		    		outputStream.write(bytesArray, 0, bytesRead);
		    	}
		    	
		    	
		    	log.info("Object {} has been downloaded.", objectName);
		    }

		} catch (AmazonS3Exception e) {
			flag = false;
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
			flag = false;
		    log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch (FileNotFoundException e) {
			// download path Not found 
			flag = false;
			log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch (IOException e) {
			// s3ObjectInputStream error
			flag = false;
			log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} finally {
			if( outputStream != null )
				try {
					outputStream.close();
				} catch (IOException e) {
					log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
				}
			
	    	if( s3ObjectInputStream != null)
				try {
					s3ObjectInputStream.close();
				} catch (IOException e) {
					log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
				}
		}
		
		return flag;
	}
	
	/**
	 * @author kyoung il pak
	 * @implNote delete object
	 * 
	 * @param bucketName
	 * @param objectName
	 *  
	 *  @return none
	 */	
	public void deleteObject( String bucketName, String objectName ) {
		// ex.
//		String bucketName = "sample-bucket";
//		String objectName = "sample-object";


		// delete object
		try {
		    s3.deleteObject(bucketName, objectName);
		    log.info("Object {} has been deleted.", objectName);
		} catch (AmazonS3Exception e) {
			log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		} catch(SdkClientException e) {
			log.error("{}", ErrorLogMessage.getPrintStackTrace(e) );
		}
		
	}
	
}
