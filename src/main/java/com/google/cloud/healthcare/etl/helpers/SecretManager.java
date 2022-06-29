package com.google.cloud.healthcare.etl.helpers;

import java.io.IOException;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

public class SecretManager {
    // Access the payload for the given secret version if one exists. The version
    // can be a version number as a string (e.g. "5") or an alias (e.g. "latest").
    public static String accessSecretVersion(String projectId, String secretId, String versionId) {
        try{
            try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
                SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
    
                // Access the secret version.
                AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
    
                return response.getPayload().getData().toStringUtf8();
            }
        }catch(IOException e){
            System.out.println("Exception while fetching the secret: " + e.getMessage());
        }
        return null;
    }
}
