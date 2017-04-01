package com.catfish.service;

import org.apache.poi.ss.usermodel.Sheet;

public interface SheetService {
	void sheetHandle(ExcelHandleInterface handleInterface, Sheet sheet);
}
