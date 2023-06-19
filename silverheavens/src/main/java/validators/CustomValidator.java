package validators;

import java.util.ArrayList;

import com.developer.silverheavens.entities.Rate;

public class CustomValidator {
	
	//validate new Rate
	public static ValidatorResult validateNewRate(Rate newRate) {
		//fields
		boolean testPassed=true;
		ArrayList<String> errorMsgs = new ArrayList<>();
		
		//nights should be > 0
		if(newRate.getNights()<=0) {
			errorMsgs.add("NIGHTS Should be a posivive mumber.");
			testPassed = false;
		}
		
		//dates should not be null
		if(newRate.getStayDateFrom()==null || newRate.getStayDateTo()==null) {
			errorMsgs.add("STAY_DATE_FROM and STAY_DATE_TO should not be NULL.");
			testPassed = false;
		}
		
		//from date should be before to date
		if(newRate.getStayDateFrom()!=null && newRate.getStayDateTo()!=null &&  newRate.getStayDateFrom().isAfter(newRate.getStayDateTo())) {
			errorMsgs.add("STAY_FROM_DATE("+newRate.getStayDateFrom()+") should be before STAY_TO_DATE "+(newRate.getStayDateTo())+". ");
			testPassed = false;
		}
		
		//value should be positive
		if(newRate.getValue()<=0) {	
			errorMsgs.add("VALUE Should be a posivive mumber.");
			testPassed = false;
		}
		
		return new ValidatorResult(testPassed, errorMsgs);
		
	}
	
	
}
