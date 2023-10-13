package com.piwotworki.app.plugins.googledrive;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
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

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleDrivePlugin {

    public String[] fetchRecipes(String accessToken, String appName) throws IOException
    {
        try {
            var token = AccessToken.newBuilder().setTokenValue(accessToken).build();
            var credential = GoogleCredentials.newBuilder().setAccessToken(token).build();
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
            Drive service = new Drive.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName(appName)
                    .build();
            var fileList = service
                    .files()
                    .list()
                    // .setQ("'appDataFolder' in parents and name = 'recipes.dump'")
                    .setQ("name = 'recipes.dump'")
                    .execute();
            var foundFiles = fileList.getFiles();
            if(foundFiles.isEmpty()) {
                return new String[] { "No file found", "NOT_OK" };
            }

            var foundFile = foundFiles.get(0);
            var id = foundFile.getId();
            var baos = new ByteArrayOutputStream();

            service.files().get(id).executeMediaAndDownloadTo(baos);
            var content = new String(baos.toByteArray(), "UTF-8");
            return new String[] {content, "OK"};
        } catch(Exception ex) {
            return new String[] {"", "Error: " + ex.getMessage() };
        }
    }

    public String storeRecipes(String recipesJson, java.io.File dumpFile, String accessToken, String appName) {
        String value = "OK";
        try {

            try (var writer = new PrintWriter(dumpFile, "UTF-8")) {
                writer.print(recipesJson);
            }

            var token = AccessToken.newBuilder().setTokenValue(accessToken).build();
            var credential = GoogleCredentials.newBuilder().setAccessToken(token).build();
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

            // Build a new authorized API client service.
            Drive service = new Drive.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName(appName)
                    .build();

            File file = new File();
            file.setName("recipes.dump");
            // file.setParents(Collections.singletonList("appDataFolder"));
            FileContent content = new FileContent("application/json", dumpFile);
            service.files().create(file, content).execute();

        } catch( Exception ex) {
            value = ex.getMessage();
        }
        Log.i("Echo", value);
        return value;
    }

    public String echo(String accessToken)  {
        String value = "gdrive";
        try {
            var token = AccessToken.newBuilder().setTokenValue(accessToken).build();
            var credential = GoogleCredentials.newBuilder().setAccessToken(token).build();
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

            // Build a new authorized API client service.
            Drive service = new Drive.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName("Piwotworki")
                    .build();

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
        } catch( Exception ex) {
            value = ex.getMessage();
        }
        Log.i("Echo", value);
        return value;
    }
}
