package com.catfish.service.impl;

import com.catfish.service.ExcelHandleInterface;
import com.catfish.service.SheetService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SheetServiceImpl implements SheetService {

	@Transactional
	@Override
	public void sheetHandle(ExcelHandleInterface handleInterface, Sheet sheet) {
		for (short i = 1; i < sheet.getLastRowNum() + 1; i++) {
			Row row = sheet.getRow(i);
			handleInterface.handle(row);
		}
	}

}
