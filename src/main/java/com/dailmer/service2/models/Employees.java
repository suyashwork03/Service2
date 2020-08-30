package com.dailmer.service2.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "employees")
@XmlAccessorType(XmlAccessType.FIELD)
public class Employees {
	@XmlElement(name = "employee")
	private List<DataModel> employeeList;

	public Employees() {
		super();
	}

	public Employees(List<DataModel> employeeList) {
		super();
		this.employeeList = employeeList;
	}

	public List<DataModel> getEmployeeList() {
		return employeeList;
	}

	public void setEmployeeList(List<DataModel> employeeList) {
		this.employeeList = employeeList;
	}

	@Override
	public String toString() {
		return "Employees [employeeList=" + employeeList + "]";
	}
	

}
