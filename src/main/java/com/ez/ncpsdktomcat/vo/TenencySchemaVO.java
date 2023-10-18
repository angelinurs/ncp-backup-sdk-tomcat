package com.ez.ncpsdktomcat.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TenencySchemaVO {
	private String schema;
	private String date;
	private String time;
	private int status;
	
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
