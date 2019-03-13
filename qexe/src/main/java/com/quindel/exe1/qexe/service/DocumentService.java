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
	
	public void rollbaclModification(String docName) {
		
		Document doc = ElasticClient.getInstance().retrieveDocument(docName);
		
		if(doc != null) {
		
			List<ChangeDocCommand>  changeCommands = ElasticClient.getInstance().retrieveDocumentChangesCommands(docName);
			
			if(changeCommands != null && changeCommands.size() > 0) {
				
				// TODO: foreach hasta numero maximo. Con y sin persistencia.
				ChangeDocCommand command = changeCommands.get(0);
				ChangeDocParams lastChgCmd = command.getParams();
				
				CHANGES_TYPES chdCmdIds = CHANGES_TYPES.values()[lastChgCmd.getCmdId()];

				switch(chdCmdIds) {
				
					case ADD_LINE:
									
						int numLastLine = doc.linesCount();
						lastChgCmd.setLineIdx(numLastLine);
						
						if(eraseDocumentLine(doc, numLastLine)){
							ElasticClient.getInstance().eraseDocumentChange(command.getId());
						}
						
						break;
						
					case MOD_LINE:
						
						if(modifyDocumentLine(doc, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt())) {
							ElasticClient.getInstance().eraseDocumentChange(command.getId());
						}
						
						break;
						
					case INS_LINE:
						
						if(eraseDocumentLine(doc, lastChgCmd.getLineIdx())){
							ElasticClient.getInstance().eraseDocumentChange(command.getId());
						}
						
						break;
						
					case DEL_LINE:
						
						if(insertLineInDocument(doc, lastChgCmd.getLineIdx(), lastChgCmd.getLineTxt())){
							ElasticClient.getInstance().eraseDocumentChange(command.getId());
						}
						
						break;
						
					default:
						
						break;
				}
			}
		}
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
	
	public String getLine(ChangeDocParams command) {
		return ElasticClient.getInstance().retrieveDocument(command.getDocName())
				.getLine(command.getLineIdx());
	}
	
	public int getNumLines(ChangeDocParams command) {
		return ElasticClient.getInstance().retrieveDocument(command.getDocName()).linesCount();
	}
	
	private synchronized boolean eraseDocumentLine(Document doc, int line) {
		
		boolean ret = false;
		
		if(doc != null && doc.eraseLine(line)) {
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
		
		command.setTimeStamp(new Date().getTime());		
		command.setCmdId(DBConstants.CHANGES_TYPES.MOD_LINE.getId());
		String oldLineValue = doc.getLine(command.getLineIdx());
		
		if(ElasticClient.getInstance().persisteDocChange(changeCommandToJson(command)))
		{
			if(doc != null && doc.modifyLine(command.getLineIdx(), command.getLineTxt())) {
				
				command.setLineTxt(oldLineValue);
			
				ElasticClient.getInstance().persistDocument(doc);
				ret = true;
			}
		}
		
		return ret;
	}
	
	private synchronized boolean modifyDocumentLine(Document doc, int lineIdx, String lineTxt) {
		boolean ret = false;
		
		if(doc != null && doc.modifyLine(lineIdx, lineTxt)) {
			
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
		boolean ret = false;
		if(doc != null && doc.insertLine(lineIdx, lineTxt)) {
			ElasticClient.getInstance().persistDocument(doc);
			ret = true;
		}
		return ret;
	}
}
