package com.developer.silverheavens.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.developer.silverheavens.entities.Rate;

public class XlsMaker<T> {
	//fields
	List<T> dataList;
	Workbook workbook;
	
	//getter setter
	public Workbook getWorkbook() {
		return workbook;
	}
	
	/*CTOR*/
	public XlsMaker(List<T> data) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		dataList = data;
		generateXml();
	}

	/*METHOD*/
	private void generateXml() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		//create work book
		workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		
		//set headers
		setHeaderRow(sheet);
		
		
		populateData(sheet);

	}
	
	//set row titles
	private void setHeaderRow(Sheet sheet) {
		//create header row
		Row headerRow =  sheet.createRow(0);
		//iterate over fields and get name
		Field fields[] = Rate.class.getDeclaredFields();
		for(int i=0;i<fields.length;i++) {
			Cell c = headerRow.createCell(i);
			c.setCellValue(fields[i].getName());
		}
	}
	
	//set data
	private void populateData(Sheet sheet) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		int rowCounter=1;
		for(T r:dataList){
			Row dataRow =  sheet.createRow(rowCounter);
			Field fields[] = Rate.class.getDeclaredFields();
			for(int i=0;i<fields.length;i++) {
				//get field name
				String fieldName = fields[i].getName();
				//make getter
				String methodName = "get"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
				//make getter method 
				Method getter = r.getClass().getDeclaredMethod(methodName);
				//call getter method
				Object valueObject = getter.invoke(r);
				String value = "";
				if(valueObject==null) {
					value="null";
				}else {
					value = valueObject.toString();
				}
				dataRow.createCell(i).setCellValue(value);
			}
			//create next row
			rowCounter++;
		};
	}
}
