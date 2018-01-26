package com.wicky.violation;

import java.io.Serializable;
import java.util.Date;

public class TestDate implements Serializable {
	private static final long serialVersionUID = -8821594207282171827L;
	
	private Integer id;
	private Date startDate;
	private Date endDate;
	private String url;
	private Date entryDatetime;
	private int entryId;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getEntryDatetime() {
		return this.entryDatetime;
	}
	public void setEntryDatetime(Date dd1) {
		this.entryDatetime =  dd1;
	}
	public int getEntryId() {
		return entryId;
	}
	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}
	
}