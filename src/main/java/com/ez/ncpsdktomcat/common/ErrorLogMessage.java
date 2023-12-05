package com.ez.ncpsdktomcat.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorLogMessage {	
	
	private ErrorLogMessage( ) {
		throw new IllegalStateException("Error Log - e - Message");
	}	
	
	public static String getPrintStackTrace(Exception e) {
        
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
         
        return errors.toString();
    }
}
