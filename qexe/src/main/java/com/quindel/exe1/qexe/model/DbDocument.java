package com.quindel.exe1.qexe.model;

import java.util.List;

public class DbDocument {

	private List<DbDocumentLine> lines;
	
	public DbDocument(String docName){
		
	}

	public void addLine(String line) {
		
	}
	
	/**
	 * @return the lines
	 */
	public List<DbDocumentLine> getLines() {
		return lines;
	}

	/**
	 * @param lines the lines to set
	 */
	public void setLines(List<DbDocumentLine> lines) {
		this.lines = lines;
	}
}
