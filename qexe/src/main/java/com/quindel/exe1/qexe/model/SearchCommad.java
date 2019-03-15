package com.quindel.exe1.qexe.model;

import java.util.ArrayList;
import java.util.List;

public class SearchCommad {

	private List<String> docNames;
	private List<String> words;
	
	public SearchCommad() {
		docNames = new ArrayList<>();
		words = new ArrayList<>();
	}
	
	/**
	 * @return the docNames
	 */
	public List<String> getDocNames() {
		return docNames;
	}
	/**
	 * @param docNames the docNames to set
	 */
	public void setDocNames(List<String> docNames) {
		this.docNames = docNames;
	}
	/**
	 * @return the words
	 */
	public List<String> getWords() {
		return words;
	}
	/**
	 * @param words the words to set
	 */
	public void setWords(List<String> words) {
		this.words = words;
	}
	
}
