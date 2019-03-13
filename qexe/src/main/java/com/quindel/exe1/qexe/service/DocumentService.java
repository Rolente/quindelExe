package com.quindel.exe1.qexe.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.quindel.exe1.qexe.database.DBConstants;
import com.quindel.exe1.qexe.database.ElasticClient;
import com.quindel.exe1.qexe.database.DBConstants.CHANGES_TYPES;
import com.quindel.exe1.qexe.model.Document;
import com.quindel.exe1.qexe.model.ChangeDocCommand;
import com.quindel.exe1.qexe.model.ChangeDocParams;

@Component
public class DocumentService {
	
	public String changeCommandToJson(ChangeDocParams command) {
		return DBConstants.GSON.toJson(command);
	}	

	public Document getDocument(String docName) {
		return ElasticClient.getInstance().retrieveDocument(docName);
	}
	
	public String getLine(ChangeDocParams command) {
		return ElasticClient.getInstance().retrieveDocument(command.getDocName())
				.getLine(command.getLineIdx());
	}
	
	public int getNumLines(ChangeDocParams command) {
		
		Document doc = getDocument(command.getDocName());
		
		if(doc != null)
			return doc.linesCount();
		else
			return 0;
	}
	
	public synchronized Document rollbaclModifications(String docName)
	{
		return rollbaclModifications(docName, 1, true);
	}
	
	public Document rollbaclModifications(String docName, int numModifications, boolean persist) {
		
		Document doc = ElasticClient.getInstance().retrieveDocument(docName);
		
		if(doc != null && numModifications > 0) {
		
			List<ChangeDocCommand>  changeCommands = ElasticClient.getInstance().retrieveDocumentChangesCommands(docName);
			
			if(changeCommands != null) {
				
				for(ChangeDocCommand command: changeCommands) {
					
					ChangeDocParams lastChgCmd = command.getParams();
					
					CHANGES_TYPES chdCmdIds = CHANGES_TYPES.values()[lastChgCmd.getCmdId()];

					switch(chdCmdIds) {
					
						case ADD_LINE:
										
							int numLastLine = doc.linesCount();
							lastChgCmd.setLineIdx(numLastLine);
							
							if(eraseDocumentLine(doc, numLastLine, persist) && persist){
								ElasticClient.getInstance().eraseDocumentChange(command.getId());
							}
							
							break;
							
						case MOD_LINE:
							
							if(modifyDocumentLine(doc, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt(), persist) && persist) {
								ElasticClient.getInstance().eraseDocumentChange(command.getId());
							}
							
							break;
							
						case INS_LINE:
							
							if(eraseDocumentLine(doc, lastChgCmd.getLineIdx(), persist) && persist){
								ElasticClient.getInstance().eraseDocumentChange(command.getId());
							}
							
							break;
							
						case DEL_LINE:
							
							if(insertLineInDocument(doc, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt(), persist) && persist){
								ElasticClient.getInstance().eraseDocumentChange(command.getId());
							}
							
							break;
							
						default:
							
							break;
					}
					
					numModifications--;
					
					if(numModifications <= 0)
						break;
				}
			}
		}
		
		return doc;
	}
	
	
	public synchronized void addLineToDocument(ChangeDocParams command) {
		
		Document doc = ElasticClient.getInstance().retrieveDocument(command.getDocName());
		
		if(doc == null) {
			doc = new Document();
			doc.setDocName(command.getDocName());
		}		
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.ADD_LINE.getId());
		
		doc.addLine(command.getLineTxt());		
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command)))		
			ElasticClient.getInstance().persistDocument(doc);			
	}
	
	private synchronized boolean eraseDocumentLine(Document doc, int line) {
		return eraseDocumentLine(doc, line, true);
	}
	
	private synchronized boolean eraseDocumentLine(Document doc, int line, boolean persist) {
		
		boolean ret = false;
		
		if(doc != null && doc.eraseLine(line)) {
			
			if(persist)
				ElasticClient.getInstance().persistDocument(doc);
			
			ret = true;
		}		
		
		return ret;
	}
	
	public synchronized void eraseDocumentLine(ChangeDocParams command) {
		Document doc = ElasticClient.getInstance().retrieveDocument(command.getDocName());
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.DEL_LINE.getId());
		command.setLineTxt(doc.getLine(command.getLineIdx()));
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command)))	
			eraseDocumentLine(doc, command.getLineIdx());
	}
	
	public synchronized boolean modifyDocumentLine(ChangeDocParams command) {
		
		boolean ret = false;
		
		Document doc = ElasticClient.getInstance().retrieveDocument(command.getDocName());
		
		String newLineTxt = command.getLineTxt();
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.MOD_LINE.getId());
		command.setLineTxt(doc.getLine(command.getLineIdx()));
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command)))
		{
			ret = modifyDocumentLine(doc, command.getLineIdx(), newLineTxt);
		}
		
		return ret;
	}
	
	private synchronized boolean modifyDocumentLine(Document doc, int lineIdx, String lineTxt) {
		return modifyDocumentLine(doc, lineIdx, lineTxt, true);
	}
	
	private synchronized boolean modifyDocumentLine(Document doc, int lineIdx, String lineTxt, boolean persist) {
		boolean ret = false;
		
		if(doc != null && doc.modifyLine(lineIdx, lineTxt)) {
			
			if(persist)
				ElasticClient.getInstance().persistDocument(doc);
			
			ret = true;
		}
		
		return ret;
	}
	
	public synchronized void insertLineInDocument(ChangeDocParams command) {
		
		Document doc = ElasticClient.getInstance().retrieveDocument(command.getDocName());
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.INS_LINE.getId());
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command))) {
			insertLineInDocument(doc, command.getLineIdx(), command.getLineTxt());
		}
	}
	
	private synchronized boolean insertLineInDocument(Document doc, int lineIdx, String lineTxt) {
		return insertLineInDocument(doc, lineIdx, lineTxt, true);
	}
	
	private synchronized boolean insertLineInDocument(Document doc, int lineIdx, String lineTxt, boolean persist) {
		boolean ret = false;
		if(doc != null && doc.insertLine(lineIdx, lineTxt)) {
			
			if(persist)
				ElasticClient.getInstance().persistDocument(doc);
			
			ret = true;
		}
		return ret;
	}
}
