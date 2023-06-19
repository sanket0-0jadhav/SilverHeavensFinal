package com.developer.silverheavens.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.developer.silverheavens.dto.ResponseDto;
import com.developer.silverheavens.dto.ResponseStatus;
import com.developer.silverheavens.entities.Rate;
import com.developer.silverheavens.exceptions.XlsCreationException;
import com.developer.silverheavens.service.RateService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/rates")
public class RateController {
	
	/*FIELDS*/
	@Autowired
	private RateService rateService;

	//To get all rates according to page and sort
//	@GetMapping("/paged")
//	public ResponseEntity<ResponseDto<List<Rate>>> getPagableRates(@RequestParam Pageable page){
//		System.out.println(page);
//		List<Rate> responseRates = rateService.getRates(false);
//		
//		//create response
//		ResponseDto<List<Rate>> resp = new ResponseDto<List<Rate>>(ResponseStatus.SUCCESS,responseRates,null);
//		return new ResponseEntity<ResponseDto<List<Rate>>>(resp,HttpStatus.OK);
//	}
	
	//To get all rates (active + inactive) --- http://localhost:9090/rates/all
	@GetMapping("/all")
	public ResponseEntity<ResponseDto<List<Rate>>> getActiveRates(){
		List<Rate> responseRates = rateService.getRates(false);
		
		//create response
		ResponseDto<List<Rate>> resp = new ResponseDto<List<Rate>>(ResponseStatus.SUCCESS,responseRates,null);
		return new ResponseEntity<ResponseDto<List<Rate>>>(resp,HttpStatus.OK);
	}
	
	//To get all active rates --- http://localhost:9090/rates/active
	@GetMapping("/active")
	public ResponseEntity<ResponseDto<List<Rate>>> getAllRates(){
		List<Rate> responseRates = rateService.getRates(true);
		
		//create response
		ResponseDto<List<Rate>> resp = new ResponseDto<List<Rate>>(ResponseStatus.SUCCESS,responseRates,null);
		return new ResponseEntity<ResponseDto<List<Rate>>>(resp,HttpStatus.OK);
	}
	
	//get rate with specific id ---- http://localhost:9090/rates/{id}
	@GetMapping("/{rateId}")
	public ResponseEntity<ResponseDto<Rate>> getRatesWithId(@PathVariable(name = "rateId") int rateId){
		Rate rateResponse = rateService.getRate(rateId);
		
		//create response
		ResponseDto<Rate> resp = new ResponseDto<Rate>(ResponseStatus.SUCCESS,rateResponse,null);
		return new ResponseEntity<ResponseDto<Rate>>(resp,HttpStatus.OK);
	}
	
	//get rates of specific bungalow ---- http://localhost:9090/rate/bungalow/{id}
	@GetMapping("/bungalow/{bungalowId}")
	public ResponseEntity<ResponseDto<List<Rate>>> getRatesWithBungalow(@PathVariable("bungalowId") int bungalowId){
		List<Rate> responseRates = rateService.getRatesByBungalowId(bungalowId);
		
		//create response
		ResponseDto<List<Rate>> resp = new ResponseDto<List<Rate>>(ResponseStatus.SUCCESS, responseRates, null);
		return new ResponseEntity<ResponseDto<List<Rate>>>(resp,HttpStatus.OK);
	}
	
	//create and insert a new rate entry --- http://localhost:9090/rate/new
	@PostMapping("/new")
	public ResponseEntity<String> createNewRate(@RequestBody Rate newRate){
		newRate.setId(0);
		if(rateService.addNewRate(newRate)) {
			return new ResponseEntity<String>("Data inserted!",HttpStatus.OK);
		}else {
			return new ResponseEntity<String>("Error in inserting.",HttpStatus.BAD_REQUEST);
		}
		
	}
	
	//update an existing rate --- http://localhost:9090/rate/update
	@PatchMapping("/update")
	public ResponseEntity<String> updateRate(@RequestBody Rate updateRate){
//		return new ResponseEntity<String>("unimplemented endpoint",HttpStatus.BAD_REQUEST);
		if(rateService.updateRate(updateRate)) {
			return new ResponseEntity<String>("Data Updated!",HttpStatus.OK);
		}else{
			return new ResponseEntity<String>("Error in updating.",HttpStatus.BAD_REQUEST);
		} 
	}
	
	//delete an existing rate --- http://localhost:9090/rate/delete/{id}
	@DeleteMapping("/delete/{rateId}")
	public ResponseEntity<String> deleteRate(@PathVariable int rateId){
		if(rateService.deleteRate(rateId)) {			
			return new ResponseEntity<String>("Deleted!",HttpStatus.OK); 			
		}else {
			return new ResponseEntity<String>("Error in Deleting.",HttpStatus.BAD_REQUEST);
		}
		//return new ResponseEntity<String>("unimplemented end point",HttpStatus.BAD_REQUEST);
	}
	
	//importing data from excel and inserting in Db
	@PostMapping("/import")
	public ResponseEntity<String> importRate(@RequestParam MultipartFile file){
		try {
			rateService.importData(file);
			return new ResponseEntity<String>("OK",HttpStatus.OK);
		}catch (Exception ex) {
			throw new XlsCreationException(ex.getMessage());
		}
		
		
		//return new ResponseEntity<String>("unimplemented endpoint",HttpStatus.BAD_REQUEST);
	}
	

	//Exporting Database data to excel
	@GetMapping("/export")
	public ResponseEntity<?> exportRate(HttpServletResponse response){
		//prepare response
		response.setContentType("application/octet-stream");
		String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=rates" + new Date() + ".xlsx";
        response.setHeader(headerKey, headerValue);
		try {
			Workbook workbook = rateService.exportData();
			ServletOutputStream outputStream = response.getOutputStream();
	        workbook.write(outputStream);
	        workbook.close();
	        outputStream.close();
	        return new ResponseEntity<String>("Done",HttpStatus.OK);
		} catch (Exception e) {
			throw new XlsCreationException(e.getMessage());
		}
	}
	
	
}
