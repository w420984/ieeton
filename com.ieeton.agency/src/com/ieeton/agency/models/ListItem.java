package com.ieeton.agency.models;

import java.io.Serializable;

public class ListItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2648824110514082977L;

	private String text;
	private boolean isChecked;
	
	public ListItem(String text, boolean checked){
		this.text = text;
		isChecked = checked;
	}
	
	public void setChecked(boolean checked){
		isChecked = checked;
	}
	
	public String getText(){
		return text;
	}
	
	public boolean isChecked(){
		return isChecked;
	}
}
