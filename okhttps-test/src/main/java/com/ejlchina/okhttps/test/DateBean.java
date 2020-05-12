package com.ejlchina.okhttps.test;

import java.util.Date;

public class DateBean {

    private int id;
    private Date date;
    
    
	public DateBean() {
	}
    
	public DateBean(int id, Date date) {
		this.id = id;
		this.date = date;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "DateBean [id=" + id + ", date=" + date + "]";
	}
	
}
