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
import com.quindel.exe1.qexe.model.DbDocumentLine;

@Component
public class DocumentService {
	
	public String changeCommandToJson(ChangeDocParams command) {
		return DBConstants.GSON.toJson(command);
	}	

	public Document getDocument(String docName) {
		List<DbDocumentLine> docLines = ElasticClient.getInstance().getDbDocumentLines(docName);
		
		if(docLines != null)
			return createDocumentFromlines(docName, docLines);
		else
			return null;
	}
	
	public DbDocumentLine getLine(ChangeDocParams command) {
		return ElasticClient.getInstance().getDbDocumentLine(command.getDocName(), command.getLineIdx());
	}
	
	public long getNumLines(ChangeDocParams command) {		
		return ElasticClient.getInstance().getDocumentLinesCount(command.getDocName());
	}
	
	public synchronized Document rollbaclModifications(String docName)
	{
		return rollbaclModifications(docName, 1, true);
	}
	
	public Document rollbaclModifications(String docName, int numModifications, boolean persist) {
		
		List<DbDocumentLine> docLines = null;
		
		if(!persist)
			docLines = ElasticClient.getInstance().getDbDocumentLines(docName);
				
		if(numModifications > 0) {
		
			List<ChangeDocCommand>  changeCommands = ElasticClient.getInstance().retrieveDocumentChangesCommands(docName);
			
			if(changeCommands != null) {
				
				for(ChangeDocCommand command: changeCommands) {
					
					ChangeDocParams lastChgCmd = command.getParams();
					
					CHANGES_TYPES chdCmdIds = CHANGES_TYPES.values()[lastChgCmd.getCmdId()];

					switch(chdCmdIds) {
					
						case ADD_LINE:
										
							if(!persist) {
								
								if(docLines != null)
									docLines.remove((int) lastChgCmd.getLineIdx() - 1);
							}
							else{
								
								DbDocumentLine delLine = new DbDocumentLine(docName, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt());
								
								if(eraseDocumentLine(delLine)) 
									ElasticClient.getInstance().eraseDocumentChange(command.getId());
							}
							
							break;
							
						case MOD_LINE:
							
							DbDocumentLine modLine = new DbDocumentLine(docName, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt());
							
							if(!persist) {
								
								if(docLines != null)
									docLines.set((int)lastChgCmd.getLineIdx() - 1, modLine);
							}
							else if(modifyDocumentLine(modLine)) 
								ElasticClient.getInstance().eraseDocumentChange(command.getId());
							
							break;
							
						case INS_LINE:
							
							if(!persist) {
								
								if(docLines != null)
									docLines.remove((int)lastChgCmd.getLineIdx() - 1);
							}
							else{
								
								DbDocumentLine eraseLine = new DbDocumentLine(docName, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt());
								
								if(eraseDocumentLine(eraseLine)) 
									ElasticClient.getInstance().eraseDocumentChange(command.getId());
							}
														
							break;
							
						case DEL_LINE:
							
							DbDocumentLine insLine = new DbDocumentLine(docName, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt());
							
							if(!persist) {
								
								if(docLines != null)
									docLines.add((int)lastChgCmd.getLineIdx() - 1, insLine);
							}
							else{
								
								if(insertLineInDocument(insLine))
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
		
		if(persist) 
			docLines = ElasticClient.getInstance().getDbDocumentLines(docName);
				
		return createDocumentFromlines(docName, docLines);
	}
	
	private Document createDocumentFromlines(String docName, List<DbDocumentLine> docLines) {
		
		Document doc = new Document();
		
		if(docName != null)
			doc.setDocName(docName);
		
		if(docLines != null) {
			for(DbDocumentLine line: docLines) {
				doc.addLine(line.getText());
			}
		}
		
		return doc;
	}
	
	public synchronized boolean addLineToDocument(ChangeDocParams command) {
		
		boolean ret = false;
		
		DbDocumentLine line = new DbDocumentLine(command.getDocName(), command.getLineIdx(),command.getLineTxt());
		line.setLineIdx((int) ElasticClient.getInstance().getDocumentLinesCount(command.getDocName()) + 1);
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.ADD_LINE.getId());
		command.setLineIdx((int)line.getLineIdx());
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command)))		
			ret = ElasticClient.getInstance().insertDocumentLine(line);
		
		return ret;
	}
	
	private synchronized boolean eraseDocumentLine(DbDocumentLine line) {
		
		boolean ret = false;
		
		if(line != null) {
			ret = ElasticClient.getInstance().eraseDocumentLine(line);
		}		
		
		return ret;
	}
	
	public synchronized boolean eraseDocumentLine(ChangeDocParams command) {
		
		boolean ret = false;
		
		DbDocumentLine line = new DbDocumentLine(command.getDocName(), command.getLineIdx(),command.getLineTxt());
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.DEL_LINE.getId());
		command.setLineTxt(ElasticClient.getInstance().getDbDocumentLine(command.getDocName(), command.getLineIdx()).getText());
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command)))	
			ret = eraseDocumentLine(line);
		
		return ret;
	}
	
	public synchronized boolean modifyDocumentLine(ChangeDocParams command) {
		
		boolean ret = false;
		
		DbDocumentLine line = new DbDocumentLine(command.getDocName(), command.getLineIdx(),command.getLineTxt());
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.MOD_LINE.getId());
		command.setLineTxt(ElasticClient.getInstance().getDbDocumentLine(command.getDocName(), command.getLineIdx()).getText());
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command)))
		{
			ret = modifyDocumentLine(line);
		}
		
		return ret;
	}
		
	private synchronized boolean modifyDocumentLine(DbDocumentLine line) {		
		return ElasticClient.getInstance().updateDocumentLine(line);
	}
	
	public synchronized boolean insertLineInDocument(ChangeDocParams command) {
		
		boolean ret = false;
		
		DbDocumentLine line = new DbDocumentLine(command.getDocName(), command.getLineIdx(),command.getLineTxt());
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.INS_LINE.getId());
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command))) {
			ret = insertLineInDocument(line);
		}
		
		return ret;
	}
		
	private synchronized boolean insertLineInDocument(DbDocumentLine line ) {
		boolean ret = false;
		if(line != null) {
			
			ret = ElasticClient.getInstance().insertDocumentLine(line);
		}
		return ret;
	}
}
