package com.piwotworki.app.plugins.googledrive;

import android.util.Log;

import androidx.annotation.NonNull;


import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.OAuth2Credentials;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Objects;

public class GoogleDrivePlugin {

    private final String DATA_FILENAME = "appData";
    private final String SYNC_FILENAME = "syncState";


    @NonNull
    private Drive getDrive(
            String accessToken,
            String appName
    ) throws Exception {


        try {

            long timestampWhenTokenGeneratedInSecond = 1678956050; // Replace this with the timestamp when access token was generated
            long tokenExpiresInSecond = 3599; // Replace this with your access token expiration time
            long expirationTimeInMS = (timestampWhenTokenGeneratedInSecond + tokenExpiresInSecond) * 1000; // Convert it to milliseconds

            var token = AccessToken.newBuilder().setTokenValue(accessToken).build();
            GoogleCredentials credential = GoogleCredentials.newBuilder().setAccessToken(token).build();
            credential.refreshIfExpired();

            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

            return new Drive.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer
            ).setApplicationName(appName)
                    .build();


        } catch (Exception ex) {
            throw new Exception("Exception while building up G-Drive Connection", ex);
        }
    }

    public long getSecondsUntilExpiration(Date expirationTime) {
        Date currentTime = new Date();
        long millisecondsUntilExpiration = expirationTime.getTime() - currentTime.getTime();
        return millisecondsUntilExpiration / 1000; // Convert milliseconds to seconds
    }

    @NonNull
    private static String[] downloadFile(Drive service, File foundFile) throws IOException {
        var id = foundFile.getId();
        var baos = new ByteArrayOutputStream();

        service.files().get(id).executeMediaAndDownloadTo(baos);
        var content = baos.toString(StandardCharsets.UTF_8);
        return new String[]{content, "OK"};
    }

    public Object[] hasAppDataOnDrive(String accessToken) throws IOException {
        try {
            Drive service = getDrive(accessToken, "Piwotworki");
            var isEmpty = queryForFile(service, DATA_FILENAME).isEmpty();

            return new Object[]{
                    isEmpty,
                    "OK"
            };
        } catch (Exception ex) {
            return new String[]{"", "Error: " + ex.getMessage()};
        }
    }

    public String[] fetchSyncState(String accessToken, String appName) throws IOException {
        try {

            if (appName.isBlank()) {
                throw new InvalidPropertiesFormatException("appName was not provided");
            }

            Drive service = getDrive(accessToken, appName);

            List<File> files;

            try {
                files = getFileList(service, SYNC_FILENAME);
                if (files == null || files.isEmpty()) {
                    return new String[]{"Sync file not found", "EMPTY"};
                }
            } catch (Exception ex) {
                throw new Exception("Failed getting sync file from drive." + ex.getMessage(), ex);
            }

            var foundFile = files.get(0);
            return downloadFile(service, foundFile);

        } catch (IllegalStateException ex) {
            return new String[]{"", "SHOULD_RELOGIN"};
        } catch (Exception ex) {

            String value = ex.getMessage();
            if (value == null || value.isEmpty()) {
                value = "StoreSyncData Exception of type " + ex.getClass().getName() + " had no information";
            }

            return new String[]{"", "Error: " + value};
        }
    }

    private List<File> getFileList(Drive service, String fileName) throws Exception {
        FileList fileListService;
        try{
            fileListService = queryForFile(service, fileName);
        }
        catch(Exception ex){
            throw new Exception(String.format("Querying for file %s failed. Msg: %s Stack: %s", fileName, ex.getMessage(), Arrays.toString(ex.getStackTrace())));
        }


        List<File> foundFiles;
        try {
            foundFiles = fileListService.getFiles();
            if (foundFiles == null) {
                return null;
            }
        } catch (Exception ex) {
            throw new Exception(String.format("Error While getting files with name: %s, Stack: %s", fileName, Arrays.toString(ex.getStackTrace())), ex);
        }
        return foundFiles;
    }

    public String[] fetchAppData(String accessToken, String appName) throws IOException {
        try {
            Drive service = getDrive(accessToken, appName);

            var foundFiles = queryForFile(service, DATA_FILENAME);
            List<File> files = foundFiles.getFiles();
            if (files == null || files.isEmpty()) {
                return new String[]{"App Data file not found", "EMPTY"};
            }

            var foundFile = files.get(0);
            return downloadFile(service, foundFile);
        } catch (IllegalStateException ex) {
            return new String[]{"", "SHOULD_RELOGIN"};
        } catch (Exception ex) {
            return new String[]{"", "Error: " + ex.getMessage()};
        }
    }

    public String storeAppData(String json, java.io.File dumpFile, String accessToken, String appName) {
        String value = "OK";
        try {

            String[] existingData = this.fetchAppData(accessToken, appName);

            String existing = "{}";
            if (Objects.equals(existingData[1], "OK")) {
                existing = existingData[0];
            }

            String updatedString = this.updateJsonObject(existing, json);

            try (var writer = new PrintWriter(dumpFile, StandardCharsets.UTF_8)) {
                writer.print(updatedString);
            } catch (Exception ex) {
                throw new Exception("Could not store data on temporary dump file", ex);
            }

            if (appName.isBlank()) {
                throw new Exception("Cannot properly initialized google drive because app name was not provided");
            }

            Drive service = getDrive(accessToken, appName);

            createOrUpdateFile(service, dumpFile, DATA_FILENAME);

        } catch (IllegalStateException ex) {
            value = "SHOULD_RELOGIN";
        } catch (Exception ex) {
            value = ex.getMessage();
            if (value == null || value.isEmpty()) {
                value = "StoreAppData Exception of type " + ex.getClass().getName() + " had no information";
            }
        }
        Log.i("Storing sync data done", value);
        return value;
    }

    private void createOrUpdateFile(Drive service, java.io.File dumpFile, String fileName) throws Exception {

        List<File> files;

        if (service == null) {
            throw new Exception("Drive was not properly provided");
        }

        if (dumpFile == null) {
            throw new Exception("Temp file was not properly provided");
        }

        if (fileName.isBlank()) {
            throw new Exception("Artifact file name was not properly provided");
        }


        try {
            files = getFileList(service, fileName);

        } catch (Exception ex) {
            String message = String.format("Failed getting %s files from drive. ErrMsg: %s", fileName, ex.getMessage());
            throw new Exception(message, ex);
        }

        if(files != null){
            try {
                for (var driveFile : files) {
                    var fileId = driveFile.getId();
                    service.files().delete(fileId).execute();
                }
            } catch (Exception ex) {
                throw new Exception("Cannot Delete files on Drive." + ex.getMessage(), ex);
            }
        }


        Thread.sleep(300);

        try {
            File file = new File();
            file.setName(String.format("%s.dump", fileName));
            file.setParents(Collections.singletonList("appDataFolder"));

            try {
                FileContent content = new FileContent("application/json", dumpFile);
                var createResponse = service.files().create(file, content).setFields("id").execute();
                Log.i("CREATE", createResponse.toString());
            } catch (Exception e) {
                Log.e("CREAtE", "Error creating file: " + e.getMessage(), e);
                throw new Exception("Error creating file: " + e.getMessage(), e);
            }
        } catch (Exception ex) {
            throw new Exception("Cannot Create file for storing" + ex.getMessage(), ex);
        }
    }

    private String updateJsonObject(String existing, String withNewValues) throws JSONException {
        // Example JSON string representing an object

        // JSON string representing the update object (can be empty)

        // Parse the original JSON string into a JSONObject
        JSONObject objToBeUpdated = new JSONObject(existing);

        // Parse the update JSON string into a JSONObject
        JSONObject objWithNewValues = new JSONObject(withNewValues);

        // Check if the objWithNewValues is not empty
        var iterator = objWithNewValues.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();

            objToBeUpdated.put(key, objWithNewValues.get(key));
        }

        // Convert the updated JSONObject back to a JSON string
        String updatedJsonString = objToBeUpdated.toString();

        // Print the updated JSON string
        System.out.println(updatedJsonString);

        return updatedJsonString;
    }

    public String storeSyncData(String syncDataJson, java.io.File dumpFile, String accessToken, String appName) {
        String value = "OK";
        try {

            String[] existingData = this.fetchSyncState(accessToken, appName);

            String existing = "{}";
            if (Objects.equals(existingData[1], "OK")) {
                existing = existingData[0];
            }

            try {
                String updatedString = this.updateJsonObject(existing, syncDataJson);

                try (var writer = new PrintWriter(dumpFile, StandardCharsets.UTF_8)) {
                    writer.print(updatedString);
                }
            } catch (Exception ex) {
                throw new Exception("Cannot wrote new json object to stream", ex);
            }

            Drive service = getDrive(accessToken, appName);

            createOrUpdateFile(service, dumpFile, SYNC_FILENAME);

        } catch (IllegalStateException ex) {
            value = "SHOULD_RELOGIN";
        } catch (Exception ex) {
            value = ex.getMessage();
            if (value == null || value.isEmpty()) {
                value = "StoreSyncData Exception of type " + ex.getClass().getName() + " had no information";
            }
        }
        Log.i("Echo", value);
        return value;
    }

    private FileList queryForFile(Drive service, String fileName) throws Exception {

        if (service == null) {
            throw new Exception("G-drive cannot be null");
        }

        return service.files().list()
                .setSpaces("appDataFolder")
                .setQ(String.format("name contains '%s' and trashed=false", fileName))
                .execute();
    }
}
