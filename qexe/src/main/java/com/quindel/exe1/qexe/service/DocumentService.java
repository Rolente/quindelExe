package com.quindel.exe1.qexe.service;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.quindel.exe1.qexe.database.ElasticClient;
import com.quindel.exe1.qexe.model.Document;

@Component
public class DocumentService {
	
	private ElasticClient elasticClient;
	
	@PostConstruct
	public void start() {
		elasticClient = new ElasticClient();
	}

	public Document getDocument(String docName) {
		return elasticClient.retrieveDocument(docName);
	}
	
	public synchronized void addLineToDocument(String docName, String lineText) {
		
		Document doc = elasticClient.retrieveDocument(docName);
		
		if(doc == null) {
			doc = new Document();
			doc.setDocName(docName);
		}		
		
		doc.addLine(lineText);

		elasticClient.persistDocument(doc);
	}
	
	public String getLine(String docName, int lineIdx) {
		return elasticClient.retrieveDocument(docName)
				.getLine(lineIdx);
	}
	
	public int getNumLines(String docName) {
		return elasticClient.retrieveDocument(docName).getNumLines();
	}
	
	public void eraseDocumentLine(String docName, int lineIdx) {
		Document doc = elasticClient.retrieveDocument(docName);
		
		if(doc != null && doc.eraseLine(lineIdx)) {
			elasticClient.persistDocument(doc);
		}		
	}
	
	public void modifyDocumentLine(String docName, String newText, int lineIdx) {
		
		Document doc = elasticClient.retrieveDocument(docName);
		
		if(doc != null && doc.modifyLine(lineIdx, newText)) {
			elasticClient.persistDocument(doc);
		}		
	}
	
	public void insertLineInDocument(String docName, String newText, int lineIdx) {
		Document doc = elasticClient.retrieveDocument(docName);
		
		if(doc != null && doc.insertLine(lineIdx, newText)) {
			elasticClient.persistDocument(doc);
		}		
	}
}
