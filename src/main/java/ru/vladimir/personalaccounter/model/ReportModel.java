package ru.vladimir.personalaccounter.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;
import ru.vladimir.personalaccounter.enums.TypeOfReport;

@Getter
@Setter
public class ReportModel {
	
	private TypeOfReport typeOfReport;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date startDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date endDate;
	
	public ReportModel(){
			
	}

	
}
