#!/bin/sh

HOST=localhost
PORT=9200

curl -XDELETE http://${HOST}:${PORT}/_template/qindel_document >/dev/null 2>&1
curl -XPUT http://${HOST}:${PORT}/_template/qindel_document -H 'Content-Type: application/json' -d '
{
	"index_patterns": "qindel_document*", 
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

		"qindel_doc": {
			"properties": {
				"lines": {
 					"type": "text", 
					 "index": true,
					 "analyzer": "es_std"
				}
			}
		}
	}
}'

curl -XDELETE http://${HOST}:${PORT}/_template/qindel_changes >/dev/null 2>&1
curl -XPUT http://${HOST}:${PORT}/_template/qindel_changes -H 'Content-Type: application/json' -d '
{
	"index_patterns": "qindel__changes*", 
	"order": 1,
 
	"settings": {
		"number_of_shards": 1,
		"number_of_replicas": 0,
		"refresh_interval": "1s",
		"index.translog.durability": "request",
    	"index.codec": "best_compression"
	},

	"mappings": {

		"qindel_changes": {
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
