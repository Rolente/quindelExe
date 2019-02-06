package com.quindel.exe1.qexe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quindel.exe1.qexe.model.Document;
import com.quindel.exe1.qexe.service.DocumentService;

@RestController
public class DocumentController {

	@Autowired
	private DocumentService docService;
	
	@GetMapping("/getDoc")
	public Document getDocument() {
		return docService.getDocument();
	}
	
	@PostMapping("/addLine/{docName}")
	public void addLine(@PathVariable String docName, @RequestBody Document docInfo) {
		
		docInfo.getLines().forEach(newLine -> {
			System.out.println("Adding new line: " + newLine);
			docService.addLineToDocument(docName, newLine);
		});
	}
	
	@GetMapping("/getLine/{docName}/{numLine}")
	public String getLine(@PathVariable String docName, @PathVariable int numLine) {
		return "{\"line\": \"" + docService.getLine(docName, numLine) + "\"}";		
	}
	
	@GetMapping("/getNumLines/{docName}")
	public String getNumLines(@PathVariable String docName) {
		return "{\"linesCount\": \"" + docService.getNumLines(docName) + "\"}";	
	}
}
