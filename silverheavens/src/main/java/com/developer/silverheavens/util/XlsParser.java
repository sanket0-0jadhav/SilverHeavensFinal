package com.developer.silverheavens.util;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.DateParser;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.developer.silverheavens.entities.Rate;
import com.developer.silverheavens.exceptions.XlsDataParsingException;

public class XlsParser {

	/*fields*/
	private List<Rate> rateDataList = new ArrayList<>();
	private MultipartFile file;

	/*GETTER*/
	public List<Rate> getRateDataList() {
		return rateDataList;
	}
	
	/*Ctor*/
	public XlsParser(MultipartFile file) throws Exception {
		this.file=file;
		parseXls();
	}

	private void parseXls() throws IOException, EvaluationException {
		//build workbook
		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		Sheet sheet = workbook.getSheetAt(0);
		//System.out.println("PHYSICAL ROWS : "+sheet.getLastRowNum());
		Iterator<Row> rowIterator = sheet.iterator();
		//iterate over all rows
		while(rowIterator.hasNext()) {
			Row r = rowIterator.next();
			Rate rateFromXls = new Rate();
			LocalDate fromDate,toDate;
			int night,value,bungalowId;
			//System.out.println("Z");
			
			if(r.getCell(0).getCellType()==CellType.BLANK || r.getCell(1).getCellType()==CellType.BLANK 
			|| r.getCell(2).getCellType()==CellType.BLANK || r.getCell(3).getCellType()==CellType.BLANK 
			|| r.getCell(4).getCellType()==CellType.BLANK) {
				workbook.close();
				throw new XlsDataParsingException("Row"+Integer.toString(r.getRowNum()),"NUMBER/DATE",CellType.BLANK.toString());
			}
			//System.out.println("A");
//			try {
				fromDate = getLocalDate(r.getCell(0));
				toDate = getLocalDate(r.getCell(1));
				
				night = getInteger(r.getCell(2));
				value = getInteger(r.getCell(3));
				bungalowId = getInteger(r.getCell(4));
//			}catch(Exception ex) {
//				
//			}
			
			//STAY_DATE_FROM	STAY_DATE_TO	NIGHTS	VALUE	BUNGALOW_ID	
			// 		0				1			  2		3			4			
			
			rateFromXls.setStayDateFrom(fromDate);
			rateFromXls.setStayDateTo(toDate);
			rateFromXls.setNights(night);
			rateFromXls.setValue(value);
			rateFromXls.setBungalowId(bungalowId);
			
			System.out.println(rateFromXls);
			
			rateDataList.add(rateFromXls);
		}
		
		System.out.println("From Excel : "+rateDataList);
		
	}
	
	private LocalDate getLocalDate(Cell c) throws EvaluationException {
		//check if valid
		if(c.getCellType()!=CellType.NUMERIC || !DateUtil.isCellDateFormatted(c) ) {
			throw new XlsDataParsingException(c.getAddress().toString(),"Date",c.getCellType().toString());
		}
		
		//convert and send
		Date date = c.getDateCellValue();
		LocalDate localDate = LocalDate.of(date.getYear()+1900,date.getMonth()+1,date.getDate());
		System.out.println("R"+localDate);
		return localDate;
	}
	
	private int getInteger(Cell c) {
		//check if valid
		if(c.getCellType()!=CellType.NUMERIC) {
		throw new XlsDataParsingException(c.getAddress().toString(),"Number",c.getCellType().toString());
}

		return (int)(c.getNumericCellValue());
	}
}
