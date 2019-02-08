package com.quindel.exe1.qexe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quindel.exe1.qexe.model.Document;
import com.quindel.exe1.qexe.model.Params;
import com.quindel.exe1.qexe.service.DocumentService;

@RestController
public class DocumentController {

	@Autowired
	private DocumentService docService;
	
	@GetMapping("/getDocument")
	public Document getDocument(@RequestBody Params command) {
		return docService.getDocument(command);
	}
	
	@PostMapping("/addLine")
	public void addLine(@RequestBody Params command) {
		docService.addLineToDocument(command);
	}
	
	@PutMapping("/insertLine")
	public void insertLine(@RequestBody Params command) {
		docService.insertLineInDocument(command);
	}

	@PutMapping("/eraseLine")
	public void eraseLine(@RequestBody Params command) {
		docService.eraseDocumentLine(command);
	}
	
	@PutMapping("/modifyLine")
	public void modifyLine(@RequestBody Params command) {
		docService.modifyDocumentLine(command);
	}
	
	@GetMapping("/getLine")
	public String getLine(@RequestBody Params command) {
		return docService.getLine(command);		
	}
	
	@GetMapping("/getNumLines")
	public int getNumLines(@RequestBody Params command) {
		return docService.getNumLines(command);	
	}
}
