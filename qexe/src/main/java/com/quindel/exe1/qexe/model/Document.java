package com.quindel.exe1.qexe.model;

import java.util.ArrayList;
import java.util.List;

public class Document {

	private String docName;
	
	private List<String> lines;
	private String newLine = "";
	
	public Document() {
		docName = "No Name";
		setLines(new ArrayList<String>());
	}
	
	public String getLine(int numLine) {
		
		String line = null;
		
		if(numLine > 0 && numLine < lines.size())		
			line = lines.get(numLine);		
		
		return line;
	}
	
	public int getDocumentLinesCount() {
		return lines.size();
	}
	
	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}

	public String getNewLine() {
		return newLine;
	}

	public void setNewLine(String newLine) {
		this.newLine = newLine;
	}
}
