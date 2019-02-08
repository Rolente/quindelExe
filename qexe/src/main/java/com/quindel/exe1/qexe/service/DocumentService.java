package com.quindel.exe1.qexe.service;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.quindel.exe1.qexe.database.ElasticClient;
import com.quindel.exe1.qexe.model.Document;
import com.quindel.exe1.qexe.model.Params;

@Component
public class DocumentService {
	
	private ElasticClient elasticClient;
	
	@PostConstruct
	public void start() {
		elasticClient = new ElasticClient();
	}

	public Document getDocument(Params command) {
		return elasticClient.retrieveDocument(command.getDocName());
	}
	
	public synchronized void addLineToDocument(Params command) {
		
		Document doc = elasticClient.retrieveDocument(command.getDocName());
		
		if(doc == null) {
			doc = new Document();
			doc.setDocName(command.getDocName());
		}		
		
		doc.addLine(command.getLineTxt());

		elasticClient.persistDocument(doc);
	}
	
	public String getLine(Params command) {
		return elasticClient.retrieveDocument(command.getDocName())
				.getLine(command.getLineIdx());
	}
	
	public int getNumLines(Params command) {
		return elasticClient.retrieveDocument(command.getDocName()).getNumLines();
	}
	
	public synchronized void eraseDocumentLine(Params command) {
		Document doc = elasticClient.retrieveDocument(command.getDocName());
		
		if(doc != null && doc.eraseLine(command.getLineIdx())) {
			elasticClient.persistDocument(doc);
		}		
	}
	
	public synchronized void modifyDocumentLine(Params command) {
		
		Document doc = elasticClient.retrieveDocument(command.getDocName());
		
		if(doc != null && doc.modifyLine(command.getLineIdx(), command.getLineTxt())) {
			elasticClient.persistDocument(doc);
		}		
	}
	
	public synchronized void insertLineInDocument(Params command) {
		Document doc = elasticClient.retrieveDocument(command.getDocName());
		
		if(doc != null && doc.insertLine(command.getLineIdx(), command.getLineTxt())) {
			elasticClient.persistDocument(doc);
		}		
	}
}
