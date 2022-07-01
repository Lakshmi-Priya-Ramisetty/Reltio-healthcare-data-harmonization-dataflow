package com.google.cloud.healthcare.etl.helpers;

import java.io.IOException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

public class ApplicationCredentials {
	public static String getPayload(String projectId, String secret, String version) {
        try{
            GoogleCredentials googleCredentials = GoogleCredentials.getApplicationDefault();
            //GoogleCredentials.fromStream(new FileInputStream("/path/to/credentials.json"));
    
            String url = String.format("https://secretmanager.googleapis.com/v1/projects/%s/secrets/%s/versions/%s:access", projectId, secret, version);
    
            HttpCredentialsAdapter credentialsAdapter = new HttpCredentialsAdapter(googleCredentials);
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credentialsAdapter);
            HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
    
            JsonObjectParser parser = new JsonObjectParser(GsonFactory.getDefaultInstance());
            request.setParser(parser);
    
            HttpResponse response = request.execute();
            String data = response.parseAsString();
            System.out.println(data);
            return data;
        }catch(Exception e){
            System.out.println("Error in accessing the key: " + e.getMessage());
        }
		return null;
	}
}
