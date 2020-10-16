package com.jiping.staging.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
public class ElasticsearchRestClient {
	
	public static RestHighLevelClient highLevelClient() {
		  //String address[]=ipAddress.split(":"); 
		  String ip = "127.0.0.1"; 
		  Integer port = 9200; 
		  org.apache.http.HttpHost httpHost = new org.apache.http.HttpHost(ip, port, "http"); 
		  RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(httpHost));
		  return restHighLevelClient;
	}
}
