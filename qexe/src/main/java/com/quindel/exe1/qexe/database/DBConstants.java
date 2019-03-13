package com.quindel.exe1.qexe.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DBConstants {

	public enum CHANGES_TYPES {
		ADD_LINE(0, "addLine"),
		MOD_LINE(1, "modLine"),
		INS_LINE(2, "indLine"),
		DEL_LINE(3, "addLine"),
		NO_CHG(4, "noChanges");
		
		int id;
		String value;
		
		public int getId() {
			return id;
		}
		
		public String getValue() {
			return value;
		}
		
		CHANGES_TYPES(int id, String value){
			this.id = id;
			this.value = value;
		}
	};
	
	public static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();
}
