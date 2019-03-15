package com.quindel.exe1.qexe.model;

import com.quindel.exe1.qexe.database.DBConstants;

public class ChangeDocParams {

	private Long timeStamp = null;
	private String docName = "";
	private String lineTxt = "";
	private long lineIdx = 0;
	private int cmdId = DBConstants.CHANGES_TYPES.NO_CHG.getId();
		
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

	public long getLineIdx() {
		return lineIdx;
	}

	public void setLineIdx(long lineIdx) {
		this.lineIdx = lineIdx;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getCmdId() {
		return cmdId;
	}

	public void setCmdId(int cmdId) {
		this.cmdId = cmdId;
	}
}
