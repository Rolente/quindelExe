package com.quindel.exe1.qexe.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import com.quindel.exe1.qexe.model.Document;

public class ElasticClient {
	
	static final String INDEX = "qindel_document";
	static final String RESTORE_INDEX = "qindel_versioning";
	static final String TYPE = "qindel_doc";

	RestHighLevelClient client = new RestHighLevelClient(
			RestClient.builder(
					new HttpHost("localhost", 9200, "http"),
					new HttpHost("localhost", 9201, "http")
					)
			);
	
	public void persistDocument(Document document) {
		IndexRequest request = new IndexRequest(INDEX, TYPE, document.getDocName());	
		
		request.source(document.linesToJson(), XContentType.JSON);
		
		try {
			IndexResponse response = client.index(request, RequestOptions.DEFAULT);
			
			ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
			
			if(shardInfo.getFailed() > 0) {
				System.out.println("Fallo al indexar la linea");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * Recupera un documento de la base de datos.
	 * @param docName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Document retrieveDocument(String docName) {
		
		Document doc = null;
		
		GetRequest request = new GetRequest(
				INDEX,
				TYPE,
				docName
				);
		
		try {
			
			if(client.exists(request, RequestOptions.DEFAULT)) {
				GetResponse response = client.get(request, RequestOptions.DEFAULT);
				
				if(response.isExists()) {
					doc = new Document();
					
					Map<String, Object> source = response.getSourceAsMap();
					
					doc.setDocName(docName);
					
					Object obj = source.get("lines");
					
					if(obj != null) {
						doc.setAllLines(new ArrayList<String>((Collection<String>)obj));
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return doc;
	}
	
	@PreDestroy
	public void stop() {
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
