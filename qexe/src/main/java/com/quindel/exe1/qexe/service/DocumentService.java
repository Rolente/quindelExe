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
		elasticClient.addDocumentLine(docName, lineText);		
	}
	
	public String getLine(String docName, int line) {
		return elasticClient.getLine(docName, line);
	}
	
	public int getNumLines(String docName) {
		return elasticClient.getNumLines(docName);
	}
}
