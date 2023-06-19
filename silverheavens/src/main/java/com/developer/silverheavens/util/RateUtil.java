package com.developer.silverheavens.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.developer.silverheavens.entities.Rate;

public class RateUtil {

	//check is merge able
	public static ArrayList<Rate> checkIfMergeable(List<Rate> allRatesList,Rate newRate) {
		//deciding splitting or merging
		
		ArrayList<Rate> mergableRateList = new ArrayList<>();
		
		for(Rate r : allRatesList) {
			LocalDate rFrom = r.getStayDateFrom();
			LocalDate rTo = r.getStayDateTo();
			
			if(newRate.getValue()==r.getValue()) {
				if(rTo.plusDays(1).isEqual(newRate.getStayDateFrom())){
					//return true;
					mergableRateList.add(r);
				}
				if(rFrom.minusDays(1).isEqual(newRate.getStayDateTo())){
					mergableRateList.add(r);
				}
			}
		}
		return mergableRateList;
	}

	//expand / shrink affected rates according to new rates
	public static ArrayList<Rate> processAffectedRates(List<Rate> newGeneratedRateList,List<Rate> affectedRatesList){
		ArrayList<Rate> historicalRates = new ArrayList<Rate>();
		
		for(int i=0;i<affectedRatesList.size();i++) {
			Rate affectedRate = affectedRatesList.get(i);
			boolean isAffectedUpdated=false;
			for(int j=0;j<newGeneratedRateList.size();j++) {
				Rate generatedRate = newGeneratedRateList.get(j);
				//if rate is not same skip
				if(affectedRate.getValue()!=generatedRate.getValue() || generatedRate.getId()==-1) {
					//affectedRate.setClosedDate(LocalDate.now());
					continue;
				}
				//System.out.println(affectedRate+"X"+generatedRate);
				//if from is same; extends and give history rates
				if(affectedRate.getStayDateFrom().isEqual(generatedRate.getStayDateFrom())) {
					if(affectedRate.getStayDateTo().isAfter(generatedRate.getStayDateTo())) {
						
						//shrink affected & get historyRate
						historicalRates.add(shrinkAffected(affectedRate, generatedRate,true));
						//remove new
						System.out.println("NULLING "+generatedRate);
						//generatedRate=null;
						generatedRate.setId(-1);
						isAffectedUpdated=true;
					}else if(affectedRate.getStayDateTo().isBefore(generatedRate.getStayDateTo())) {
						//expand affected
						expandAffected(affectedRate, generatedRate,true);
						//remove new
						System.out.println("NULLING "+generatedRate);
						//generatedRate=null;
						generatedRate.setId(-1);
						isAffectedUpdated=true;
					}else {
						//affectedRate.setValue(generatedRate.getValue());
						System.out.println("UNHANDLED==========================>");
					}
				}
				//if to is same; extends and give history rates
				else if(affectedRate.getStayDateTo().isEqual(generatedRate.getStayDateTo())) {
					if(affectedRate.getStayDateFrom().isBefore(generatedRate.getStayDateFrom())) {
						//shrink affected & get historyRate
						historicalRates.add(shrinkAffected(affectedRate, generatedRate,false));
						//remove new
						System.out.println("NULLING "+generatedRate);
						//generatedRate=null;
						generatedRate.setId(-1);
						isAffectedUpdated=true;
					}else if(affectedRate.getStayDateFrom().isAfter(generatedRate.getStayDateFrom())) {
						//expand affected
						expandAffected(affectedRate, generatedRate,false);
						//remove new
						System.out.println("NULLING "+generatedRate);
						//generatedRate=null;
						generatedRate.setId(-1);
						isAffectedUpdated=true;
					}else {
						//affectedRate.setValue(generatedRate.getValue());
						System.out.println("UNHANDLED==========================>");
					}
				}else {
					System.out.println("****************");
				}
				
			}
			
			if(!isAffectedUpdated) {
				affectedRate.setClosedDate(LocalDate.now());
			}
		}
		return historicalRates;
	}
	
	//MERGE 
	public static ArrayList<Rate> MergeExternal(ArrayList<Rate> dataForDB,ArrayList<Rate> mergableRateList) {
		ArrayList<Rate> updateClosedDate = new ArrayList<>();
		dataForDB.addAll(mergableRateList);
		Collections.sort(dataForDB);
		int i=0;
		/*
		 * TODO - IF NEXT = NEW REMOVE/CLOSE
		 * */
		while(i<dataForDB.size()) {
			//if next not available stop
			if(i+1>=dataForDB.size()) {
				break;
			}
			//get current and next
			Rate currentRate = dataForDB.get(i);
			Rate nextRate = dataForDB.get(i+1);
			//is rate not same skip
			if(currentRate.getValue()!=nextRate.getValue() 
					|| currentRate.getClosedDate()!=null
					|| nextRate.getClosedDate()!=null) {
				i++;
				continue;
			}
			
			//if to and from are side side
			if(currentRate.getStayDateTo().plusDays(1).isEqual(nextRate.getStayDateFrom())) {
				//update current rate
				currentRate.setStayDateTo(nextRate.getStayDateTo());
				updateClosedDate.add(dataForDB.remove(i+1));
				i=0;
			}else {
				i++;
			}
		}
		
		
		return updateClosedDate;
	}
	
	//check if insert is redundant 
	public static boolean isRedundant(Rate newRate,List<Rate> dataFromDb) {
		LocalDate newFrom = newRate.getStayDateFrom();
		LocalDate newTo = newRate.getStayDateTo();
		
		//get filtered
		Optional<Rate> rateOptional = dataFromDb.stream()
		.filter((r)->r.getValue()==newRate.getValue())
		.filter((r)->{
			LocalDate rFrom = r.getStayDateFrom();
			LocalDate rTo = r.getStayDateTo();
			
			if(newFrom.isEqual(rFrom) && newTo.isEqual(rTo)) {
				return true;
			}else if((newFrom.isAfter(rFrom) || newFrom.isEqual(rFrom)) && (newTo.isBefore(rTo) || newTo.isEqual(rTo))) {
				return true;
			}
			return false;})
		.findFirst();
		
		//if any matches, redundant
		return rateOptional.isPresent();
	}
	
	
	//shrink
	private static Rate shrinkAffected(Rate affected,Rate generated,boolean isFromSame) {
		Rate historyRate = new Rate();
		//populate history
		historyRate.setBungalowId(generated.getBungalowId());
		historyRate.setClosedDate(null);
		historyRate.setNights(generated.getNights());
		//decide how to modify 
		if(isFromSame) {
			historyRate.setStayDateFrom(generated.getStayDateTo().plusDays(1));
			historyRate.setStayDateTo(affected.getStayDateTo());
		}else {
			historyRate.setStayDateFrom(affected.getStayDateFrom());
			historyRate.setStayDateTo(generated.getStayDateFrom().minusDays(1));
		}
		historyRate.setValue(affected.getValue());
		
		//update affected
		if(isFromSame) {
			affected.setStayDateTo(generated.getStayDateTo());
		}else {
			affected.setStayDateFrom(generated.getStayDateFrom());
			
		}
		//System.out.println("After shrink "+affected+"X"+generated);
		return historyRate;
	}
	
	//expand
	private static void expandAffected(Rate affected,Rate generated,boolean isFromSame) {
		if(isFromSame) {
			affected.setStayDateTo(generated.getStayDateTo());
		}else {
			affected.setStayDateFrom(generated.getStayDateFrom());
		}
	}
	
	//check if insert is redundent
	
	
}
