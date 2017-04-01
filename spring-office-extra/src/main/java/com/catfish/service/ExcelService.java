package com.catfish.service;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

public interface ExcelService {

	String improtExcel(String filename, InputStream inputStream, ExcelHandleInterface handleInterface, String message, String mailAddress);
	
	void exportExcel2007(HttpServletRequest request, String controllerName, String methodName, String head, String replaceParams
            , String fileName, String mailAddress) throws Exception;
//	void sheetHandle(ExcelHandleInterface handleInterface, Sheet sheet);
}
