package com.quindel.exe1.qexe.model;

public class ChangeDocCommand {

	private String id;
	private ChangeDocParams params;
	
	public ChangeDocCommand(String id, ChangeDocParams params) {
		this.id = id;
		this.params = params;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ChangeDocParams getParams() {
		return params;
	}
	public void setParams(ChangeDocParams params) {
		this.params = params;
	}
}
