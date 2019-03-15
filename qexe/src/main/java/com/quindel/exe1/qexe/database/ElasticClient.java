package com.quindel.exe1.qexe.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.quindel.exe1.qexe.model.ChangeDocCommand;
import com.quindel.exe1.qexe.model.ChangeDocParams;
import com.quindel.exe1.qexe.model.DbDocumentLine;

public class ElasticClient {
	
	private static ElasticClient elasticClient = null;
	
	static final String INDEX = "doc_line_idx";
	static final String CHG_INDEX = "doc_changes_idx";	
	
	static final String TYPE = "doc_line_type";
	static final String CHG_TYPE = "doc_changes_type";

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
	
	/***
	 * Realiza una búsqueda en uno o mas documentos una o mas palabras.
	 * @param docNames
	 * @param words
	 * @return
	 */
	public List<DbDocumentLine> searchWords(List<String> docNames, List<String> words){
		
		List<DbDocumentLine> lines = new ArrayList<>();
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(INDEX);
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
				
		if(docNames.size() == 0) {
			sourceBuilder.query(
					QueryBuilders
					 .boolQuery()
						.must(QueryBuilders.termsQuery("text", words))
				).size(DBConstants.MAX_DOC_LINES);
		}
		else {
			sourceBuilder.query(
					QueryBuilders
					 .boolQuery()
						.must(QueryBuilders.termsQuery("text", words))
						.must(QueryBuilders.termsQuery("docName", docNames))
					
				).size(DBConstants.MAX_DOC_LINES);
		}
		
		searchRequest.source(sourceBuilder);		
		
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			
			if(response.getFailedShards() == 0) {
				
				lines = new ArrayList<DbDocumentLine>();
				
				SearchHits hits = response.getHits();
				
				SearchHit[] searchHits = hits.getHits();	

				for(SearchHit hit: searchHits) {
					lines.add(lines.size(), DBConstants.GSON.fromJson(hit.getSourceAsString(), DbDocumentLine.class));
				}
			}
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lines;
	}
		
	/***
	 * Retorna el número de lineas que tienes un documento.
	 * @param docName
	 * @return
	 */
	public long getDocumentLinesCount(String docName) {
		
		long linesCount = 0;
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(INDEX);
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		sourceBuilder.query(QueryBuilders.termQuery("docName", docName));
		sourceBuilder.size(0);
		
		searchRequest.source(sourceBuilder);
		
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			
			if(response.getFailedShards() == 0) 
				linesCount = response.getHits().totalHits;
			
		} catch (IOException e) {
			
			linesCount = -1;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return linesCount;
	}
	
	/**
	 * Se inserta una linea el el documento.
	 * @param docLine
	 * @return
	 */
	public boolean insertDocumentLine(DbDocumentLine docLine) {
		boolean ok = true;
		long totalLines = getDocumentLinesCount(docLine.getDocName());
		long lineInsert = docLine.getLineIdx();
		
		// Se actualizan los números de linea de las posteriores lineas.
		if(lineInsert <= totalLines) {
			
			long actualLineIdx = totalLines;
			
			while(lineInsert <= actualLineIdx) {

				DbDocumentLine lineToUpdate = getDbDocumentLine(docLine.getDocName(), actualLineIdx);
								
				String lineToUpdateId = getDocumentLineId(lineToUpdate.getDocName(), lineToUpdate.getLineIdx());
						
				lineToUpdate.setLineIdx(lineToUpdate.getLineIdx() + 1);
				
				ok = updateDocumentLine(lineToUpdate, lineToUpdateId);
				
				if(!ok)
					break;
			
				actualLineIdx--;
			}
		}
		
		if(ok) {
			ok = false;
			
			IndexRequest request = new IndexRequest(INDEX, TYPE);	
			
			DbDocumentLine dbDocumentLine = new DbDocumentLine(docLine.getDocName(), docLine.getLineIdx(), docLine.getText());
			
			request.source(DBConstants.GSON.toJson(dbDocumentLine), XContentType.JSON);
			request.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
			
			try {
				IndexResponse response = client.index(request, RequestOptions.DEFAULT);
				
				ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
				
				if(shardInfo.getFailed() == 0) {
					ok = true;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		return ok;
	}
	
	/***
	 * Retorna una linea de un documento.
	 * @param docName
	 * @param line
	 * @return
	 */
	public DbDocumentLine getDbDocumentLine(String docName, long line) {
		DbDocumentLine dbDocLine = null;
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(INDEX);
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		
		sourceBuilder.query(
			QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery("docName", docName))
				.filter(QueryBuilders.termQuery("lineIdx", line))
		).size(DBConstants.MAX_DOC_LINES);
		
		searchRequest.source(sourceBuilder);
		
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			
			if(response.getFailedShards() == 0) {
				
				SearchHits hits = response.getHits();
				
				SearchHit[] searchHits = hits.getHits();	

				if(searchHits.length == 1) {
					dbDocLine = DBConstants.GSON.fromJson(searchHits[0].getSourceAsString(), DbDocumentLine.class);
				}
			}
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dbDocLine;
	}
	
	/***
	 * Retorna todas las lineas de un documento.
	 * @param docName
	 * @param line
	 * @return
	 */
	public List<DbDocumentLine> getDbDocumentLines(String docName) {
		List<DbDocumentLine> dbDocLines = null;
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(INDEX);
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		sourceBuilder.query(QueryBuilders.termQuery("docName", docName));
		sourceBuilder.sort("lineIdx", SortOrder.ASC);
		sourceBuilder.size(DBConstants.MAX_DOC_LINES);
		
		searchRequest.source(sourceBuilder);
		
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			
			if(response.getFailedShards() == 0) {
				
				dbDocLines = new ArrayList<DbDocumentLine>();
				
				SearchHits hits = response.getHits();
				
				SearchHit[] searchHits = hits.getHits();	

				for(SearchHit hit: searchHits) {
					dbDocLines.add(DBConstants.GSON.fromJson(hit.getSourceAsString(), DbDocumentLine.class));
				}
			}
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dbDocLines;
	}
	
	private String getDocumentLineId(String docName, long line) {
		String id = "";
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(INDEX);
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		sourceBuilder.query(QueryBuilders.boolQuery()
								.filter(QueryBuilders.termQuery("docName", docName))
								.filter(QueryBuilders.termQuery("lineIdx", line))).size(DBConstants.MAX_DOC_LINES);
		
		searchRequest.source(sourceBuilder);
		
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			
			if(response.getFailedShards() == 0) {
				
				SearchHits hits = response.getHits();
				
				SearchHit[] searchHits = hits.getHits();	

				if(searchHits.length == 1) {
					id = searchHits[0].getId();
				}
			}
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return id;
	}
	
	/***
	 * Actualiza el texto de una linea de un documento
	 * @param docName
	 * @param line
	 * @param newText
	 */
	public boolean updateDocumentLine(DbDocumentLine docLine) {
		
		boolean ret = false;
		
		String documentLineId = getDocumentLineId(docLine.getDocName(), docLine.getLineIdx());
		
		if(documentLineId.compareTo("") != 0) {
		
			IndexRequest request = new IndexRequest(INDEX, TYPE, documentLineId);	
			
			request.source(DBConstants.GSON.toJson(docLine), XContentType.JSON);
			request.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
			
			try {
				IndexResponse response = client.index(request, RequestOptions.DEFAULT);
				
				ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
				
				if(shardInfo.getFailed() == 0) {
					ret = true;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	/***
	 * Actualiza el texto de una linea de un documento de un id concreto
	 * @param docName
	 * @param line
	 * @param newText
	 */
	public boolean updateDocumentLine(DbDocumentLine docLine, String documentLineId) {
		
		boolean ret = false;
		
		if(documentLineId.compareTo("") != 0) {
		
			IndexRequest request = new IndexRequest(INDEX, TYPE, documentLineId);	
			
			request.source(DBConstants.GSON.toJson(docLine), XContentType.JSON);
			request.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
			
			try {
				IndexResponse response = client.index(request, RequestOptions.DEFAULT);
				
				ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
				
				if(shardInfo.getFailed() == 0) {
					ret = true;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	/**
	 * Elimina una linea del documento.
	 * @param docLine
	 * @return
	 */
	public boolean eraseDocumentLine(DbDocumentLine docLine) {
		boolean ret = false;
		
		long totalLines = getDocumentLinesCount(docLine.getDocName());
		long lineDel = docLine.getLineIdx();
		
		String documentLineId = getDocumentLineId(docLine.getDocName(), lineDel);
		
		DeleteRequest request = new DeleteRequest(INDEX, TYPE, documentLineId);
		request.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
		
		try {
			DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
			
			if(response.getShardInfo().getFailed() == 0) {
				ret = true;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Se actualiza el número de línea de las restantes lineas del documento.
		if(ret && lineDel < totalLines) {
			
			long actualLineIdx = lineDel;
			
			while(actualLineIdx < totalLines) {

				DbDocumentLine lineToUpdate = getDbDocumentLine(docLine.getDocName(), ++actualLineIdx);
								
				String lineToUpdateId = getDocumentLineId(lineToUpdate.getDocName(), lineToUpdate.getLineIdx());
						
				lineToUpdate.setLineIdx(lineToUpdate.getLineIdx() - 1);
				
				updateDocumentLine(lineToUpdate, lineToUpdateId);				
			}
		}
		
		return ret;
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
	
	public List<ChangeDocCommand> retrieveDocumentChangesCommands(String docName) {
		
		List<ChangeDocCommand>  changeDocCommands = null;
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(CHG_INDEX);
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		sourceBuilder.query(QueryBuilders.termQuery("docName", docName));
		sourceBuilder.sort("timeStamp", SortOrder.DESC);
		sourceBuilder.size(DBConstants.MAX_DOC_LINES);
		
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
		request.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
		
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
