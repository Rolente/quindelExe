package com.quindel.exe1.qexe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quindel.exe1.qexe.model.Document;
import com.quindel.exe1.qexe.service.DocumentService;

@RestController
public class DocumentController {

	@Autowired
	private DocumentService docService;
	
	@GetMapping("/getDocument")
	public Document getDocument(@RequestBody CommandParams command) {
		return docService.getDocument(command.getDocName());
	}
	
	@PostMapping("/addLine")
	public void addLine(@RequestBody CommandParams command) {
		docService.addLineToDocument(command.getDocName(), command.getLineTxt());
	}
	
	@PutMapping("/insertLine")
	public void insertLine(@RequestBody CommandParams command) {
		docService.insertLineInDocument(command.getDocName(), command.getLineTxt(), command.getLineIdx());
	}

	@PutMapping("/eraseLine")
	public void eraseLine(@RequestBody CommandParams command) {
		docService.eraseDocumentLine(command.getDocName(), command.getLineIdx());
	}
	
	@PutMapping("/modifyLine")
	public void modifyLine(@RequestBody CommandParams command) {
		docService.modifyDocumentLine(command.getDocName(), command.getLineTxt(), command.getLineIdx());
	}
	
	@GetMapping("/getLine")
	public String getLine(@RequestBody CommandParams command) {
		return docService.getLine(command.getDocName(), command.getLineIdx());		
	}
	
	@GetMapping("/getNumLines")
	public int getNumLines(@RequestBody CommandParams command) {
		return docService.getNumLines(command.getDocName());	
	}
}
