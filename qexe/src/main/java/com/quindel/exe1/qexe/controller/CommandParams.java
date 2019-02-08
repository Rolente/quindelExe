package com.quindel.exe1.qexe.controller;

public class CommandParams {

	private String docName = "";
	private String lineTxt = "";
	private int lineIdx = 0;
	
	private int line = 0;
	
	public int getLine() {
		return line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}
	
	String getDocName() {
		return docName;
	}

	void setDocName(String docName) {
		this.docName = docName;
	}

	String getLineTxt() {
		return lineTxt;
	}

	void setLineTxt(String lineTxt) {
		this.lineTxt = lineTxt;
	}

	public int getLineIdx() {
		return lineIdx;
	}

	public void setLineIdx(int lineIdx) {
		this.lineIdx = lineIdx;
	}
}
