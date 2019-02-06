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
	
	@GetMapping("/getDoc/{docName}")
	public Document getDocument(@PathVariable String docName) {
		return docService.getDocument(docName);
	}
	
	@PostMapping("/addLine")
	public void addLine(@RequestBody Document docInfo) {
		docService.addLineToDocument(docInfo.getDocName(), docInfo.getNewLine());
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
