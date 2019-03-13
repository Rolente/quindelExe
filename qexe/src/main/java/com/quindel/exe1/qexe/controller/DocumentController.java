package com.quindel.exe1.qexe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quindel.exe1.qexe.model.Document;
import com.quindel.exe1.qexe.model.ChangeDocParams;
import com.quindel.exe1.qexe.service.DocumentService;
@RestController
public class DocumentController {

	@Autowired
	private DocumentService docService;
	
	@GetMapping("/getDocument")
	public Document getDocument(@RequestBody ChangeDocParams command) {
		return docService.getDocument(command.getDocName());
	}
	
	@PostMapping("/addLine")
	public Document addLine(@RequestBody ChangeDocParams command) {
		docService.addLineToDocument(command);
		return docService.getDocument(command.getDocName());
	}
	
	@PostMapping("/rollbackMod/{docName}")
	public Document rollbackMod(@PathVariable String docName) {
		docService.rollbaclModification(docName);
		return docService.getDocument(docName);
	}
	
	@PutMapping("/insertLine")
	public Document insertLine(@RequestBody ChangeDocParams command) {
		docService.insertLineInDocument(command);
		return docService.getDocument(command.getDocName());
	}

	@PutMapping("/eraseLine")
	public Document eraseLine(@RequestBody ChangeDocParams command) {
		docService.eraseDocumentLine(command);
		return docService.getDocument(command.getDocName());
	}
	
	@PutMapping("/modifyLine")
	public Document modifyLine(@RequestBody ChangeDocParams command) {
		docService.modifyDocumentLine(command);
		return docService.getDocument(command.getDocName());
	}
	
	@GetMapping("/getLine")
	public String getLine(@RequestBody ChangeDocParams command) {
		return docService.getLine(command);		
	}
	
	@GetMapping("/getNumLines")
	public int getNumLines(@RequestBody ChangeDocParams command) {
		return docService.getNumLines(command);	
	}
}
