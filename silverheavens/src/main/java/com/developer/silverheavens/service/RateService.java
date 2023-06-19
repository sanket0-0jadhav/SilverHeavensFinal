package com.developer.silverheavens.service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.developer.silverheavens.entities.Bungalow;
import com.developer.silverheavens.entities.Rate;
import com.developer.silverheavens.exceptions.IdNotFoundException;
import com.developer.silverheavens.exceptions.NewRateValidationException;
import com.developer.silverheavens.exceptions.UpdateException;
import com.developer.silverheavens.exceptions.XlsCreationException;
import com.developer.silverheavens.repository.BungalowRepo;
import com.developer.silverheavens.repository.RateRepository;
import com.developer.silverheavens.specifications.BungalowSpecification;
import com.developer.silverheavens.specifications.RateSpecification;
import com.developer.silverheavens.util.RateMerger;
import com.developer.silverheavens.util.RateSplitter;
import com.developer.silverheavens.util.RateUtil;
import com.developer.silverheavens.util.XlsMaker;
import com.developer.silverheavens.util.XlsParser;

import jakarta.persistence.EntityManager;
import validators.CustomValidator;
import validators.ValidatorResult;

@Service
public class RateService {

	/*fields*/
	@Autowired
	private RateRepository rateRepo;
	@Autowired
	private BungalowRepo bungalowRepo;
	@Autowired
	private EntityManager entityManager;
	
	//send all rates OR active Rates
	public List<Rate> getRates(boolean selectActive) {
		//List<Rate> rateDtoList = new ArrayList<Rate>();
		List<Rate> rateList = null;
		
		//get data from DB
		if(selectActive)
			rateList = rateRepo.findAll(RateSpecification.closedIsNull());
//			rateList = rateRepo.findByClosedDateNull();
		else
			rateList = rateRepo.findAll();
		
		//convert to DTO
		//rateList.forEach((r)->rateDtoList.add(new Rate(r)));
		return rateList;
	}
	
	//send specific rate entry
	public Rate getRate(int rateId) {
		//get from DB
		//Optional<Rate> rateOptional = rateRepo.findById(rateId);
		Optional<Rate> rateOptional = rateRepo.findOne(RateSpecification.byRateId(rateId));
		
		//if object not found throw exception
		if(rateOptional.isEmpty())
		 throw new IdNotFoundException(Rate.class,rateId);
		
		//convert to DTO and return
		Rate rate = rateOptional.get();
		return rate;
	}
	
	//send specific bungalow
	public List<Rate> getRatesByBungalowId(int bungalowId) {
		List<Rate> rateList = new ArrayList<Rate>();
		
		//check if we have bungalow ID
		//Optional<Bungalow> bungalowOptional =  bungalowRepo.findById(bungalowId);
		Optional<Bungalow> bungalowOptional = bungalowRepo.findOne(BungalowSpecification.withId(bungalowId));
		
		//throw exception if bungalow id is not present
		if(bungalowOptional.isEmpty())
			throw new IdNotFoundException(Bungalow.class, bungalowId);
		
		//get rate entries
		//rateList = rateRepo.findByBungalowId(bungalowId);
		rateList = rateRepo.findAll(RateSpecification.byBungalowId(bungalowId));	
		//convert to DTO and return
		//rateList.forEach((r)->rateDtoList.add(new RateDto(r)));
		
		return rateList;
	}
	
	//add new Rate
	public boolean addNewRate(Rate newRate) {
		/*Validation*/
		ValidatorResult result = CustomValidator.validateNewRate(newRate);
		if(!result.isValidationPassed()) {
			throw new NewRateValidationException(result.getMessage().toString());
		}
		
		//check if bungalow exists
//		if(!bungalowRepo.existsById(newRate.getBungalowId())) {
//			throw new IdNotFoundException(Bungalow.class, newRate.getBungalowId());
//		}
		//throw exception if bungalow id is not present
		Optional<Bungalow> bungalowOptional = bungalowRepo.findOne(BungalowSpecification.withId(newRate.getBungalowId()));
		if(bungalowOptional.isEmpty())
			throw new IdNotFoundException(Bungalow.class, newRate.getBungalowId());
		
		/*VAR*/
//		List<Rate> allRatesList = rateRepo.findByBungalowIdAndClosedDateNull(newRate.getBungalowId());
//		List<Rate> allRatesList = rateRepo.findAll(RateSpecification.byBungalowId(newRate.getBungalowId())
//									.and(RateSpecification.closedIsNull()));
//		List<Rate> allRatesList = rateRepo.findAll(RateSpecification.byBungalowId(newRate.getBungalowId())
//				.and(RateSpecification.closedIsNull()
//				.and(
//					(RateSpecification.beforeOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(RateSpecification.afterOrEqualToTo(newRate.getStayDateTo().minusDays(1))))
//					.or(RateSpecification.afterOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(RateSpecification.beforeOrEqualToTo(newRate.getStayDateTo().minusDays(1))))
//					.or(RateSpecification.beforeOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(RateSpecification.afterOrEqualToTo(newRate.getStayDateFrom().minusDays(1))))
//					.or(RateSpecification.afterOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(RateSpecification.afterOrEqualToTo(newRate.getStayDateTo().minusDays(1))))
//				)
//				.and(RateSpecification.byNights(newRate.getNights()))));
		
		List<Rate> allRatesList = rateRepo.findAll(RateSpecification.getRatesForInsertion(newRate));
		
		
		System.out.println("All fetched : "+allRatesList);
		List<Rate> affectedRatesList = new ArrayList<>();
		List<Rate> newGeneratedRatesList;
		ArrayList<Rate> historyRateList = new ArrayList<>();
		ArrayList<Rate> mergableRateList;
		ArrayList<Rate> updateClosedDate = new ArrayList<>();
		boolean isMerging = false;
		Collections.sort(allRatesList);
		
		//redundant insert
		if(RateUtil.isRedundant(newRate, allRatesList)) {
			System.out.println("Redundant entry");
			return true;
		}
		
		//if no data present : Insert
		if(allRatesList.size()==0) {
			rateRepo.save(newRate);
			entityManager.clear();
			return true;
		}
		
		//check if merge able
		mergableRateList = RateUtil.checkIfMergeable(allRatesList, newRate);
		isMerging = mergableRateList.size()!=0;
		
		/*SPLITTING*/ 
		RateSplitter splitter = new RateSplitter(newRate,allRatesList);
		
		//get new Rates to insert
		newGeneratedRatesList = splitter.getNewRatesList();
		affectedRatesList = splitter.getAffectedRates();
		
		//internal merger
		RateMerger internalMerge = new RateMerger(newGeneratedRatesList);
		newGeneratedRatesList = internalMerge.getMergedList();
		
		//COREMR CASE
		if(affectedRatesList.size()==1 && newGeneratedRatesList.size()==3) {
			System.out.println("into corner case");
			//SAVE TO DB
			newGeneratedRatesList.forEach((r)->{
				rateRepo.save(r);
				entityManager.clear();
			});
			
			//SAVE CLOSED HOSTORY
			affectedRatesList.forEach((r)->{
				r.setClosedDate(LocalDate.now());
				rateRepo.save(r);
				entityManager.clear();
			});
			return true;
		}
	
		//if(affectedRatesList.size()>0) //WORKING
		historyRateList = RateUtil.processAffectedRates(newGeneratedRatesList,affectedRatesList);
		
		//PREPARE DATA FOR DB = affected + active new
		ArrayList<Rate> dataForDB = new ArrayList<>();
		dataForDB.addAll(affectedRatesList);
		newGeneratedRatesList.forEach((r)->{
			if(r.getId()!=-1)
				dataForDB.add(r);
		});
		
		//CHECK IF MERGA
		if(isMerging) {
			updateClosedDate = RateUtil.MergeExternal(dataForDB, mergableRateList);
		}
		
		//COMMENT THIS IF CODE BREAKES ------------------------------------>>>>>>>
//		updateClosedDate.forEach((r)->{                                 //->>>>>>>
//			if(r.getId()!=0) {											//->>>>>>>
//				r.setClosedDate(LocalDate.now());
//				rateRepo.save(r);										//->>>>>>>
//			}
//		});																//->>>>>>>
		//COMMENT THIS IF CODE BREAKES ------------------------------------>>>>>>>
		
		//SAVE TO DB
		dataForDB.forEach((r)->{
			rateRepo.save(r);
			System.out.println("SAVE : "+r);
			entityManager.clear();
		});
		
		//SAVE CLOSED HOSTORY
		historyRateList.forEach((r)->{
			r.setClosedDate(LocalDate.now());
			rateRepo.save(r);
			System.out.println("ENTER CLOSED : "+r);
			entityManager.clear();
		});
	
		return true;
	}
	
	//UPDATE

	//update rate
	public boolean updateRate(Rate updateRate) {
		/*Validation*/
		ValidatorResult result = CustomValidator.validateNewRate(updateRate);
		if(!result.isValidationPassed()) {
			throw new NewRateValidationException(result.getMessage().toString());
		}
		
		//check if bungalow exists
		Optional<Bungalow> bungalowOptional = bungalowRepo.findOne(BungalowSpecification.withId(updateRate.getBungalowId()));
		if(bungalowOptional.isEmpty())
			throw new IdNotFoundException(Bungalow.class, updateRate.getBungalowId());
		
		/*----ID IS DELETED----*/
		//Get from Optional
		//Optional<Rate> refFromDbOptions = rateRepo.findById(updateRate.getId());
		Optional<Rate> refFromDbOptions = rateRepo.findOne(RateSpecification.byRateId(updateRate.getId()));
		
		//get by 
		if(refFromDbOptions.isEmpty()) {
			throw new IdNotFoundException(Rate.class, updateRate.getId());
		}
		
		Rate refFromDb = refFromDbOptions.get();
		
		if(refFromDb.getClosedDate()!=null) {
			throw new UpdateException(updateRate.getId(),"Rate is already closed");
		}
		
		//delete from DB
		rateRepo.delete(refFromDb);
		entityManager.clear();
		
		updateRate.setId(refFromDb.getId());
		System.out.println("UPDATING : "+updateRate);
		return this.addNewRate(updateRate);
		
	}
	
	//DELETE
	public boolean deleteRate(int rateId) {
		//Get from Optional
		//Optional<Rate> refFromDbOptions = rateRepo.findById(rateId);
		Optional<Rate> refFromDbOptions = rateRepo.findOne(RateSpecification.byRateId(rateId));
				
		//get by 
		if(refFromDbOptions.isEmpty()) {
			throw new IdNotFoundException(Rate.class, rateId);
		}
		
		Rate refFromDb = refFromDbOptions.get();
		
		if(refFromDb.getClosedDate()!=null) {
			throw new UpdateException(rateId,"Rate is already closed");
		}
		
		//delete from DB
		rateRepo.delete(refFromDb);
		entityManager.clear();
		
		return true;
	}
	
	//EXPORT DATE
	
	/*EXPORT*/
	public Workbook exportData() {
		//get data to export
		List<Rate> dataList = getRates(false);
		Workbook workbook;
		
		//make excel
		try {
			XlsMaker<Rate> xlsMaker = new XlsMaker<>(dataList);
			workbook = xlsMaker.getWorkbook();
		}catch(Exception ex) {
			throw new XlsCreationException(ex.getMessage());
		}
		
		System.out.println(dataList);
		//return workbook
		return workbook;
		
	}
	
	//IMPORT DATA
	
	/*IMPORT and insert*/
	public void importData(MultipartFile file) throws Exception{
		//extract rate from xls
		XlsParser parser = null;;
		
		parser = new XlsParser(file);
		
		List<Rate> ratesFromExcel = parser.getRateDataList();
		
		//iterate and add
		ratesFromExcel.forEach((r)->{			
			addNewRate(r);
		});
		
//		InputStream inputStream = file.getInputStream();
//		Workbook workbook = new XSSFWorkbook(inputStream);
//		Sheet sheet = workbook.getSheetAt(0);
//		Map<Integer,List<String>> data = new HashMap<>();
//		for(Row row :sheet) {
//			data.put(row.getRowNum(), new ArrayList<>());
//			for(Cell c : row) {
//				System.out.println(c+"-"+c.getCellType());
//				switch (c.getCellType()) {
//					case NUMERIC:
//						if (DateUtil.isCellDateFormatted(c)) {
//							System.out.println("date");
//							data.get(row.getRowNum()).add(c.getDateCellValue()+"");
//						} else {
//							System.out.println("number");
//							data.get(row.getRowNum()).add(Double.toString(c.getNumericCellValue()));
//						}
//						
//					break;
//					case STRING:
//						data.get(row.getRowNum()).add(c.getStringCellValue());
//					break;
//					default:
//						data.get(row.getRowNum()).add(" ");
//					break;
//				}
//				
//			}
//		}
//		
//		System.out.println(data);
//		
//		workbook.close();
		
	}
}
