package com.developer.silverheavens.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.developer.silverheavens.entities.Rate;

public class RateMerger {
	private List<Rate> unmergedRateList;
	//private List<Rate> mergedRateList;
	//private List<Rate> ratesClosedAfterMerging;
	
	
	public RateMerger(List<Rate> unmergedRateList) {
		this.unmergedRateList = unmergedRateList;
		//this.ratesClosedAfterMerging = new ArrayList<>();
		

		 mergeRates();

	}
	
//	public List<Rate> getRatesClosedAfterMerging(){
//		return ratesClosedAfterMerging;
//	}
	

	//check for any merging 
	public List<Rate> getMergedList(){
		return unmergedRateList;
	}
	private void mergeRates() {
		//System.out.println("1");
		//SORT
		Collections.sort(unmergedRateList);
		//System.out.println("A : "+unmergedRateList);
		//System.out.println("Before Merge : "+unmergedRateList);
		//System.out.println("Bf Merge : "+unMergedRates);
		//System.out.println("2");
		int i=0;
		while(i<unmergedRateList.size()) {
			//variable to store current and next to check mergin
			Rate currentRateSegment;
			Rate nextRateSegment;
				
			//break look if there is no next element 
			if(i+1>=unmergedRateList.size()) {
				//System.out.println("Break at "+i);
				break;
			}
			//get reference to rates
			currentRateSegment = unmergedRateList.get(i);
			nextRateSegment = unmergedRateList.get(i+1);
			//System.out.println("CHECKING : "+currentRateSegment+"--"+nextRateSegment);
			//check if merge able => if next day of currentSeg.To == nextSeg.From && values are same
			if(currentRateSegment.getStayDateTo().plusDays(1).isEqual(nextRateSegment.getStayDateFrom())
					&& currentRateSegment.getValue()==nextRateSegment.getValue()
					&& currentRateSegment.getClosedDate()==null 
					&& nextRateSegment.getClosedDate()==null) {
				//Update current.to date
				//currentRateSegment.setStayDateTo(nextRateSegment.getStayDateTo());
				Rate newMergedRate = new Rate();
				newMergedRate.setNights(currentRateSegment.getNights());
				newMergedRate.setStayDateFrom(currentRateSegment.getStayDateFrom());
				newMergedRate.setStayDateTo(nextRateSegment.getStayDateTo());
				newMergedRate.setValue(currentRateSegment.getValue());
				newMergedRate.setBungalowId(currentRateSegment.getBungalowId());
				newMergedRate.setClosedDate(null);
				
				
				//remove nextRateSeg OR put in affected and remove
				//Rate removedRate = unmergedRateList.remove(i+1);
				
				//REMOVE/UPDATE CURRENT
//				if(currentRateSegment.getId()!=0) {
//					//it is not newly creates rate ==> close this rate
//					Rate removedRate = unmergedRateList.remove(i);
//					//removedRate.setClosedDate(LocalDate.now());//CHANGED
//					ratesClosedAfterMerging.add(removedRate);
//				}else {
					//if id==0, it is newly created rate ==> remove it
					unmergedRateList.remove(currentRateSegment);
//				}
				

				//System.out.println("after curreny removal"+unmergedRateList);
				
				//REMOVE/UPDATE NEXT
//				if(nextRateSegment.getId()!=0) {
//					//it is not newly creates rate ==> close this rate
//					Rate removedRate = unmergedRateList.remove(i+1);
//					//removedRate.setClosedDate(LocalDate.now());
//					ratesClosedAfterMerging.add(removedRate);
//				}else {
					//if id==0, it is newly created rate ==> remove it
					//unmergedRateList.remove(i+1);
					unmergedRateList.remove(nextRateSegment);
//				}
//				if(ratesListFromDb.contains(removedRate)) {
//					affectedSegments.add(removedRate);
//				}
//				System.out.println("MERGED INTO "+newMergedRate);
				unmergedRateList.add(newMergedRate);
				Collections.sort(unmergedRateList);
//				System.out.println("AFTER MERGE"+unmergedRateList);
				//start iteration from top
				i=0;
				//System.out.println("Again");
			}else {
				//System.out.println("10");
				i++;
//				System.out.println("NEXT");
			}
				
		}
		Collections.sort(unmergedRateList);
		//System.out.println("After Merge : "+unmergedRateList);
		//return unmergedRateList;
	}
	
//	public static void processAffected(List<Rate> affectedRatesList,List<Rate> generatedRateList) {
//		
//		for(int i=0;i<affectedRatesList.size();i++) {
//			for(int j=0;j<generatedRateList.size();j++) {
//				Rate affectedRate = affectedRatesList.get(i);
//				Rate generatedRate = generatedRateList.get(j);
//				//System.out.println("MERGING = "+affectedRate+" & "+generatedRate);
//				//if values are not equal; SKIP
//				if(affectedRate.getValue()!=generatedRate.getValue() || affectedRate.getClosedDate()!=null) {
//					affectedRate.setClosedDate(LocalDate.now());
//					//System.out.println("VALUES NOT EQUAL");
//					continue;
//				}
//				//if from date is same
//				if(affectedRate.getStayDateFrom().isEqual(generatedRate.getStayDateFrom())) {
//					//System.out.println("FROM IS EQUAL : "+affectedRate+" & "+generatedRate);
//					//update To date
//					affectedRate.setStayDateTo(generatedRate.getStayDateTo());
//					//remove new entry
//					generatedRateList.remove(generatedRate);
//					//reset
//					i=0;
//					j=0;
//					continue;
//				}
//				if(affectedRate.getStayDateTo().isEqual(generatedRate.getStayDateTo())) {
//					//System.out.println("TO IS EQUAL : "+affectedRate+" & "+generatedRate);
//					//update from date
//					affectedRate.setStayDateFrom(generatedRate.getStayDateFrom());
//					//remove new entry
//					generatedRateList.remove(generatedRate);
//					//reset
//					i=0;
//					j=0;
//					continue;
//				}
//			}
//		}
//	}
	
}
