package com.ez.ncpsdktomcat.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.s3.model.Bucket;
import com.ez.ncpsdktomcat.service.ObjectStorageS3;

@RestController
@RequestMapping("/s3")
public class S3ObjectStorageController {
	
	@Autowired
	private ObjectStorageS3 objectStorageS3;
	
	
	@GetMapping("/list/bucket" )
	public String getBucketList() {
		List<Bucket> buckets = objectStorageS3.getBucketList();
		
		StringBuilder sb = new StringBuilder();

		for( Bucket bucket : buckets ) {
			sb.append( bucket.getName() ).append( "\n" );
		}
		
		return sb.toString();
	}
	
	@GetMapping("/list/object/{bucketName}" )
	public void getObjectList( @PathVariable String bucketName ) {
		
		objectStorageS3.getObjectList( bucketName );
	}
	
	@GetMapping("/create/{bucketName}" )
	public void createBucket( @PathVariable String bucketName ) {
		bucketName = bucketName.replace( "_", "-" );
		
		objectStorageS3.createBucket( bucketName );
	}
	
	@GetMapping("/delete/{bucketName}" )
	public void deleteBucket( @PathVariable String bucketName ) {
		
		objectStorageS3.deleteBucket( bucketName );
	}
	
	@GetMapping("/delete/object/{bucketName}/{objectName}" )
	public void deleteObject( 
			@PathVariable String bucketName,
			@PathVariable String objectName ) {
		
		objectStorageS3.deleteObject(bucketName, objectName);
	}
	
	@GetMapping("/upload/{bucketName}/{objectName}" )
	public void uploadObject( 
			@PathVariable String bucketName,
			@PathVariable String objectName ) {
//		String filePath = "/home/naru/temp";
		String schemaName = "psm_sc_svc171";
		String filename = "psm_sc_svc171.2023-09-22T16:11:27.tar.gz.enc";
		String filePath = String.format( "/home/naru/temp/temp/%s", filename );
		String objectFolderName = "schemas/";

//		String bucketName = schemaName.replace( "_", "-" );
		objectStorageS3.uploadObject( bucketName, objectFolderName, objectFolderName+objectName, filePath);
	}
	
	@GetMapping("/download/{bucketName}/{objectFolder}/{objectName}" )
	public void downloadObject( 
			@PathVariable String bucketName,
			@PathVariable String objectName,
			@PathVariable String objectFolder ) {
		String downloadPath = "/home/naru/temp/download";
		
		objectStorageS3.downloadObject(bucketName, objectFolder+"/"+objectName, downloadPath+"/"+objectName);
	}
	
	
}
