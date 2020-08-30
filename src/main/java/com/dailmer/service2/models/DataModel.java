package com.dailmer.service2.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DataModel {

	private String name;
	private String dob;
	private String salary;
	private String age;

	public DataModel() {
		super();
	}

	public DataModel(String name, String dob, String salary, String age) {
		super();
		this.name = name;
		this.dob = dob;
		this.salary = salary;
		this.age = age;
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	@XmlElement
	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	@XmlElement
	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

}
