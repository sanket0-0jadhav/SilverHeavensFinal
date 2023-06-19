package com.developer.silverheavens.specifications;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;

import com.developer.silverheavens.entities.Rate;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface RateSpecification {

	//get with id
	public static Specification<Rate> byRateId(int rateId){
		return new Specification<Rate>() {

			@Override
			public Predicate toPredicate(Root<Rate> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("id"), rateId);
			}
		};
	}
	
	//get all active rates
	public static Specification<Rate> closedIsNull(){
		return (root,query,cb)->{return cb.isNull(root.get("closedDate"));};
		//return (root,query,cb)->{return cb.equal(root.get("closedDate"), null);};
	}
	
	//get all active rates
	public static Specification<Rate> byBungalowId(int bungalowId){
		return (root,query,cb)->{return cb.equal(root.get("bungalowId"), bungalowId);};
	}
	
	//get after from date
	public static Specification<Rate> afterOrEqualToFrom(LocalDate from){
		return (root,query,cb)->{return cb.greaterThanOrEqualTo(root.get("stayDateFrom"), from);};
	}
	
	//get only before date
	public static Specification<Rate> beforeOrEqualToFrom(LocalDate from){
		return (root,query,cb)->{return cb.lessThanOrEqualTo(root.get("stayDateFrom"), from);};
	}
	
	//get after from date
	public static Specification<Rate> afterOrEqualToTo(LocalDate to){
		return (root,query,cb)->{return cb.greaterThanOrEqualTo(root.get("stayDateTo"), to);};
	}
		
	//get only before date
	public static Specification<Rate> beforeOrEqualToTo(LocalDate to){
		return (root,query,cb)->{return cb.lessThanOrEqualTo(root.get("stayDateTo"), to);};
	}
	
	//get only before date
	public static Specification<Rate> byNights(int nights){
		return (root,query,cb)->{return cb.equal(root.get("nights"), nights);};
	}
	
	//Get Required rates
	public static Specification<Rate> getRatesForInsertion(Rate newRate){
		return Specification.where(byBungalowId(newRate.getBungalowId())
				.and(closedIsNull()
				.and((beforeOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(afterOrEqualToTo(newRate.getStayDateTo().minusDays(1))))
					.or(afterOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(beforeOrEqualToTo(newRate.getStayDateTo().minusDays(1))))
					.or(beforeOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(afterOrEqualToTo(newRate.getStayDateFrom().minusDays(1))))
					.or(afterOrEqualToFrom(newRate.getStayDateFrom().plusDays(1)).and(afterOrEqualToTo(newRate.getStayDateTo().minusDays(1))))
				).and(byNights(newRate.getNights()))));
	}
}
