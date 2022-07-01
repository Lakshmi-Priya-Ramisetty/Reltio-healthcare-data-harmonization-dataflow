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

public class SecretManager {
	public static String getSecretValue(String projectId, String secret, String version) {
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
            String responseData = response.parseAsString();
            
            //Parsing the json payload
            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
            JsonObject payloadJson = jsonObject.get("payload").getAsJsonObject();
            String base64EncodedData = payloadJson.get("data").getAsString();
            
            //Converting the base64 encoded string to string
            byte[] decodedString = Base64.getDecoder().decode(new String(base64EncodedData).getBytes("UTF-8"));
            String secretValue = new String(decodedString);
            
            return secretValue;
        }catch(Exception e){
            System.out.println("Error in accessing the key: " + e.getMessage());
        }
		return null;
	}
}
