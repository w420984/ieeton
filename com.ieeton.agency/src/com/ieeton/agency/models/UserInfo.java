package com.ieeton.agency.models;

import java.io.Serializable;

public class UserInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6076283726029464940L;
	public static int ACCOUNT_DOCTOR = 0;
	public static int ACCOUNT_PATIENT = 1;
	
	private Doctor doctor;
	private Patient patient;
	private int accountType;
	
	public UserInfo(Patient patient){
		accountType = ACCOUNT_PATIENT;
		this.patient = patient;
	}
	
	public UserInfo(Doctor doctor){
		accountType = ACCOUNT_DOCTOR;
		this.doctor = doctor;
	}
	
	public Patient getPatient(){
		return patient;
	}
	
	public Doctor getDoctor(){
		return doctor;
	}
	
	public int getUserType(){
		return accountType;
	}
}
