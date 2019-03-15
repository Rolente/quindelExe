package com.quindel.exe1.qexe.model;

public class DbDocumentLine {

	private String docName = "";
	private long lineIdx = 0;
	private String text = "";
	
	public DbDocumentLine(String docName, long lineIdx, String text) {
		this.docName = docName;
		this.lineIdx = lineIdx;
		this.text = text;
	}
	/**
	 * @return the docName
	 */
	public String getDocName() {
		return docName;
	}
	/**
	 * @param docName the docName to set
	 */
	public void setDocName(String docName) {
		this.docName = docName;
	}
	/**
	 * @return the lineIdx
	 */
	public long getLineIdx() {
		return lineIdx;
	}
	/**
	 * @param lineIdx the lineIdx to set
	 */
	public void setLineIdx(long lineIdx) {
		this.lineIdx = lineIdx;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	
	
}
