package com.ez.ncpsdktomcat.vo;

import java.nio.file.Paths;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenencySchemaVO {
	private String key;
	
	private String sourcePath;
	private String objectPath;
	private String fileName;
	
	private String absolutePath;
	
	private String schema;
	private String date;
	private String time;
	private int status;
	
	public TenencySchemaVO( String schema, String date, String time ) {
		this.schema = schema.replace( "_", "-" );
		this.date = date;
		this.time = time.split( "[.]" )[0];
		this.status = 0;
		this.sourcePath = "/app/util/temp";
		this.objectPath = "schema/";
		this.fileName = String.format( "%s.%sT%s.tar.gz.enc", this.schema, this.date, this.time );
		this.absolutePath = Paths.get( this.sourcePath, this.fileName )
				                 .toAbsolutePath()
				                 .toString();
	}
	
	@Override
	public String toString() {
		return String.format("SCHEMA : %20S, DATE : %s, TIME : %S, STATUS : %d",
				this.getSchema(),
				this.getDate(),
				this.getTime(),
				this.getStatus()
				); 
	}
}
