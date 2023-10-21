package com.piwotworki.app.plugins.googledrive;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GoogleDrivePlugin {

    private final String DATA_FILENAME = "appData";
    private final String SYNC_FILENAME = "syncState";

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

            Drive service = getDrive(accessToken, appName);

            var syncFiles = queryForFile(service, SYNC_FILENAME);
            List<File> files = syncFiles.getFiles();
            if (files.isEmpty()) {
                return new String[]{"Sync file not found", "EMPTY"};
            }

            var foundFile = files.get(0);
            return downloadFile(service, foundFile);

        } catch (Exception ex) {
            return new String[]{"", "Error: " + ex.getMessage()};
        }
    }

    public String[] fetchAppData(String accessToken, String appName) throws IOException {
        try {
            Drive service = getDrive(accessToken, appName);

            var foundFiles = queryForFile(service, DATA_FILENAME);
            List<File> files = foundFiles.getFiles();
            if (files.isEmpty()) {
                return new String[]{"App Data file not found", "EMPTY"};
            }

            var foundFile = files.get(0);
            return downloadFile(service, foundFile);
        } catch (Exception ex) {
            return new String[]{"", "Error: " + ex.getMessage()};
        }
    }

    @NonNull
    private static Drive getDrive(String accessToken, String appName) {
        var token = AccessToken.newBuilder().setTokenValue(accessToken).build();
        var credential = GoogleCredentials.newBuilder().setAccessToken(token).build();
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
        return new Drive.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName(appName)
                .build();
    }

    @NonNull
    private static String[] downloadFile(Drive service, File foundFile) throws IOException {
        var id = foundFile.getId();
        var baos = new ByteArrayOutputStream();

        service.files().get(id).executeMediaAndDownloadTo(baos);
        var content = new String(baos.toByteArray(), "UTF-8");
        return new String[]{content, "OK"};
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

            try (var writer = new PrintWriter(dumpFile, "UTF-8")) {
                writer.print(updatedString);
            }

            Drive service = getDrive(accessToken, appName);

            var fileName = DATA_FILENAME;
            createOrUpdateFile(dumpFile, service, fileName);

        } catch (Exception ex) {
            value = ex.getMessage();
        }
        Log.i("Storing sync data done", value);
        return value;
    }

    private FileList listFiles(Drive service){
        try {
            return service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageSize(100)
                    .execute();
        } catch (IOException e) {
            Log.e("CREATE", "QUERY LIST ERROR", e);
            return new FileList();
        }
    }

    private void createOrUpdateFile(java.io.File dumpFile, Drive service, String fileName) throws IOException, InterruptedException {

        var foundFiles = queryForFile(service, fileName);
        List<File> files = foundFiles.getFiles();

        for (var driveFile : files) {
            var fileId = driveFile.getId();
            service.files().delete(fileId).execute();
        }

        Thread.sleep(300);

        File file = new File();
        file.setName(String.format("%s.dump", fileName));
        file.setParents(Collections.singletonList("appDataFolder"));

        try {
            FileContent content = new FileContent("application/json", dumpFile);
            var createResponse = service.files().create(file, content).setFields("id").execute();
            Log.i("CREATE", createResponse.toString());
        } catch (IOException e) {
            Log.e("CREAtE", "Error creating file: " + e.getMessage(), e);
            throw e;
        }
    }

    private String updateJsonObject(String existing, String withNewValues) {
        // Example JSON string representing an object
        String jsonExisting = existing;

        // JSON string representing the update object (can be empty)
        String jsonWithNewValues = withNewValues;

        try {
            // Parse the original JSON string into a JSONObject
            JSONObject objToBeUpdated = new JSONObject(jsonExisting);

            // Parse the update JSON string into a JSONObject
            JSONObject objWithNewValues = new JSONObject(jsonWithNewValues);

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

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonExisting;
    }

    public String storeSyncData(String syncDataJson, java.io.File dumpFile, String accessToken, String appName) {
        String value = "OK";
        try {

            String[] existingData = this.fetchSyncState(accessToken, appName);

            String existing = "{}";
            if (Objects.equals(existingData[1], "OK")) {
                existing = existingData[0];
            }

            String updatedString = this.updateJsonObject(existing, syncDataJson);

            try (var writer = new PrintWriter(dumpFile, "UTF-8")) {
                writer.print(updatedString);
            }

            Drive service = getDrive(accessToken, appName);

            createOrUpdateFile(dumpFile, service, SYNC_FILENAME);

        } catch (IllegalStateException ex) {
            value = "SHOULD_RELOGIN";
        } catch (Exception ex) {
            value = ex.getMessage();
        }
        Log.i("Echo", value);
        return value;
    }

    private FileList queryForFile(Drive service, String fileName) throws IOException {

        return service.files().list()
                .setSpaces("appDataFolder")
                .setQ(String.format("name = '%s.dump'", fileName))
                .execute();
    }

    public String echo(String accessToken) {
        String value = "gdrive";
        try {
            Drive service = getDrive(accessToken, "Piwotworki");

            var diskFile = new java.io.File("/data/user/0/com.piwotworki.app/files/INSTALLATION");
            File file = new File();
            file.setName("Hello, world.txt");
            FileContent content = new FileContent("text/plain", diskFile);
            service.files().create(file, content).execute();
/*
            List<File> files = new ArrayList<File>();

            String pageToken = null;
            do {
                FileList result = service.files().list()
                        // .setQ("mimeType='image/jpeg'")
                        .setSpaces("drive")
                        // .setFields("nextPageToken, items(id, title)")
                        // .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    System.out.printf("Found file: %s (%s)\n",
                            file.getName(), file.getId());
                }

                files.addAll(result.getFiles());

                pageToken = result.getNextPageToken();
            } while (pageToken != null);

            value = "Files found: " + files.size();
*/
        } catch (Exception ex) {
            value = ex.getMessage();
        }
        Log.i("Echo", value);
        return value;
    }
}
