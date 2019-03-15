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
import com.quindel.exe1.qexe.model.DbDocumentLine;
import com.quindel.exe1.qexe.service.DocumentService;
@RestController
public class DocumentController {

	@Autowired
	private DocumentService docService;
	
	@GetMapping("/getDocument")
	public Document getDocument(@RequestBody ChangeDocParams command) {
		
		Document doc = docService.getDocument(command.getDocName());
		
		if(doc != null)
			return doc;
		else
			return new Document();
	}
	
	@PostMapping("/addLine")
	public Document addLine(@RequestBody ChangeDocParams command) {
		
		if(docService.addLineToDocument(command)) {
			Document doc = docService.getDocument(command.getDocName());
			
			if(doc != null)
				return doc;
		}
		
		return new Document();
	}
	
	@PutMapping("/insertLine")
	public Document insertLine(@RequestBody ChangeDocParams command) {
		
		if(docService.insertLineInDocument(command)) {
			Document doc = docService.getDocument(command.getDocName());
			
			if(doc != null)
				return doc;
		}
		
		return new Document();
	}

	@PutMapping("/eraseLine")
	public Document eraseLine(@RequestBody ChangeDocParams command) {
		if(docService.eraseDocumentLine(command)) {
			Document doc = docService.getDocument(command.getDocName());
			
			if(doc != null)
				return doc;
		}
		
		return new Document();
	}
	
	@PutMapping("/modifyLine")
	public Document modifyLine(@RequestBody ChangeDocParams command) {
		if(docService.modifyDocumentLine(command)) {
			Document doc = docService.getDocument(command.getDocName());
			
			if(doc != null)
				return doc;
		}

		return new Document();
	}
	
	@PostMapping("/rollbackDocMod/{docName}")
	public Document rollbackMod(@PathVariable String docName) {
		return docService.rollbaclModifications(docName);
	}
	
	@GetMapping("/seeDocPrevVersion/{docName}/{numModifications}")
	public Document seeDocPrevVersion(@PathVariable String docName, @PathVariable int numModifications) {
		return docService.rollbaclModifications(docName, numModifications, false);
	}
		
	@GetMapping("/getLine")
	public DbDocumentLine getLine(@RequestBody ChangeDocParams command) {
		return docService.getLine(command);		
	}
	
	@GetMapping("/getNumLines")
	public long getNumLines(@RequestBody ChangeDocParams command) {
		return docService.getNumLines(command);	
	}
}
