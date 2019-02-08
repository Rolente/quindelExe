package com.quindel.exe1.qexe.model;

public class Params {

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
	
	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getLineTxt() {
		return lineTxt;
	}

	public void setLineTxt(String lineTxt) {
		this.lineTxt = lineTxt;
	}

	public int getLineIdx() {
		return lineIdx;
	}

	public void setLineIdx(int lineIdx) {
		this.lineIdx = lineIdx;
	}
}
