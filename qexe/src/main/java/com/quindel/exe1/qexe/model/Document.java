package com.quindel.exe1.qexe.model;

import java.util.ArrayList;
import java.util.List;

import com.quindel.exe1.qexe.database.DBConstants;

class DocumentLines{

	private List<String> lines;
	
	public DocumentLines() {
		lines = new ArrayList<String>();
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}
}

public class Document {

	private String docName = "Not set";
	
	private DocumentLines documentLines = new DocumentLines();
	
	public String getLine(int numLine) {
		
		String line = null;
		
		if(numLine > 0 && numLine <= getDocumentLines().getLines().size())		
			line = getDocumentLines().getLines().get(numLine - 1);		
		
		return line;
	}
	
	public int linesCount() {
		return getDocumentLines().getLines().size();
	}
	
	public void setAllLines(List<String> lines) {
		getDocumentLines().setLines(lines);
	}
	
	public String linesToJson() {
		return DBConstants.GSON.toJson(getDocumentLines());
	}
	
	public void addLine(String lineTxt) {
		getDocumentLines().getLines().add(lineTxt);
	}
	
	public boolean insertLine(int numLine, String lineTxt) {
		if(numLine > 0)	{
			getDocumentLines().getLines().add(numLine - 1, lineTxt);
			return true;
		}
		return false;
	}
	
	public boolean eraseLine(int numLine) {
		if(numLine > 0 && numLine <= getDocumentLines().getLines().size())	{
			getDocumentLines().getLines().remove(numLine - 1);
			return true;
		}
		return false;
	}
	
	public boolean modifyLine(int lineIdx, String lineTxt) {
		if(eraseLine(lineIdx)) {
			insertLine(lineIdx, lineTxt);
			return true;
		}
		return false;
	}
	
	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public DocumentLines getDocumentLines() {
		return documentLines;
	}
}
