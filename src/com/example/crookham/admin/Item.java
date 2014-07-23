package com.example.crookham.admin;

public class Item {

	private String field;
	private String location;
	private String variety;
	private String url;
	private String userid;

	public Item(String field, String variety, String location, String muUrl, String muUid) {
		super();
		this.field = field;
		this.variety = variety;
		this.location = location;
		this.url = muUrl;
		this.userid=muUid;
	}

	public String getField() {
		return field;
	}

	public void setTitle(String title) {
		this.field = title;
	}

	public String getVariety() {
		return variety;
	}

	public void setVariety(String variety) {
		this.variety = variety;
	}

	public String getLocation() {

		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getUrl() {

		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUid() {

		return userid;
	}

	public void setUid(String uid) {
		this.userid=uid;
	}

}

