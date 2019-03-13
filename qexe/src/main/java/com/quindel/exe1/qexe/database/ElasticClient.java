package com.quindel.exe1.qexe.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.quindel.exe1.qexe.model.ChangeDocCommand;
import com.quindel.exe1.qexe.model.ChangeDocParams;
import com.quindel.exe1.qexe.model.Document;

public class ElasticClient {
	
	private static ElasticClient elasticClient = null;
	
	static final String INDEX = "qindel_document";
	static final String CHG_INDEX = "qindel_changes";	
	
	static final String TYPE = "qindel_doc";
	static final String CHG_TYPE = "qindel_changes";

	RestHighLevelClient client = null;
	
	private ElasticClient() {
		client = new RestHighLevelClient(
				RestClient.builder(
						new HttpHost("localhost", 9200, "http"),
						new HttpHost("localhost", 9201, "http")
						)
				);
	}
	
	public static ElasticClient getInstance() {
		 
		if(elasticClient == null)
			elasticClient = new ElasticClient();
		
		return elasticClient;
	}
	
	public boolean persisteDocChange(String changeCmdStr) {
		
		boolean ret = false;
		IndexRequest request = new IndexRequest(CHG_INDEX, CHG_TYPE);	
		
		request.source(changeCmdStr, XContentType.JSON);
		
		try {
			IndexResponse response = client.index(request, RequestOptions.DEFAULT);
			
			ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
			
			if(shardInfo.getFailed() > 0) {
				System.out.println("Fallo al indexar el comando de modifcacion");
			}
			else
				ret = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return ret;
	}
	
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
	
	public List<ChangeDocCommand> retrieveDocumentChangesCommands(String docName) {
		
		List<ChangeDocCommand>  changeDocCommands = null;
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(CHG_INDEX);
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		sourceBuilder.query(QueryBuilders.termQuery("docName", docName));
		sourceBuilder.sort("timeStamp", SortOrder.DESC);
		
		searchRequest.source(sourceBuilder);
		
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			
			if(response.getFailedShards() == 0) {
				
				changeDocCommands = new ArrayList<ChangeDocCommand>();
				
				SearchHits hits = response.getHits();
				
				SearchHit[] searchHits = hits.getHits();	
				
				for(SearchHit hit: searchHits) {
					changeDocCommands.add(new ChangeDocCommand(hit.getId(), DBConstants.GSON.fromJson(hit.getSourceAsString(), ChangeDocParams.class)));
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return changeDocCommands;
	}
		
	public boolean eraseDocumentChange(String docId) {
		
		boolean ret = false;
		
		DeleteRequest request = new DeleteRequest(CHG_INDEX, CHG_TYPE, docId);
		
		try {
			DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
			
			if(response.getShardInfo().getFailed() == 0) {
				ret = true;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
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
