package com.yang.activiti.model;

import java.io.Serializable;

public class Page implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long count;
	
	private Object data;
	
	public Page() {
		super();
	}

	public Page(long count, Object data) {
		super();
		this.count = count;
		this.data = data;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
