#!/bin/sh

HOST=localhost
PORT=9200

curl -XDELETE http://${HOST}:${PORT}/_template/doc_line_idx >/dev/null 2>&1
curl -XPUT http://${HOST}:${PORT}/_template/doc_line_idx -H 'Content-Type: application/json' -d '
{
	"index_patterns": "doc_line_idx*", 
	"order": 1,
 
	"settings": {
		"number_of_shards": 1,
		"number_of_replicas": 0,
		"refresh_interval": "1s",
		"index.translog.durability": "request",
    	"index.codec": "best_compression",
		"analysis":{
			"analyzer":{
				"es_std":{
					"type": "standard",
					"stopwords": "_spanish_"
				}
			}			
		} 
	},

	"mappings": {

		"doc_line_type": {
			"properties": {
				"docName": {
					"type": "keyword",
					"index": true
				},
				"text": {
 					"type": "text", 
					 "index": true,
					 "analyzer": "es_std"
				},
				"lineIdx": {
					"type": "long"
				}
			}
		}
	}
}'

curl -XPUT http://${HOST}:${PORT}/doc_line_idx

curl -XDELETE http://${HOST}:${PORT}/_template/doc_changes_idx >/dev/null 2>&1
curl -XPUT http://${HOST}:${PORT}/_template/doc_changes_idx -H 'Content-Type: application/json' -d '
{
	"index_patterns": "doc_changes_idx*", 
	"order": 1,
 
	"settings": {
		"number_of_shards": 1,
		"number_of_replicas": 0,
		"refresh_interval": "1s",
		"index.translog.durability": "request",
    	"index.codec": "best_compression"
	},

	"mappings": {

		"doc_changes_type": {
			"properties": {
				"timeStamp": {
					"type": "date",
					"format": "epoch_millis"
				},
				"docName": {
					"type": "keyword",
					"index": true
				},
				"lineTxt": {
					"type": "keyword",
					"index": true
				},
				"lineIdx": {
					"type": "long"
				},
				"cmdId": {
					"type": "long"
				}
			}
		}
	}
}'

curl -XPUT http://${HOST}:${PORT}/doc_changes_idx
