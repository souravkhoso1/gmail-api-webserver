package org.example;

import com.google.api.client.auth.oauth2 .Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.*;

import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GmailQuickstart {
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = System.getenv("CLEANUP_TOKEN_DIR");
    //private static final String CREDENTIALS_FILE_PATH = "/src/main/config/client_id_client_secret_2023-05-27.json";
    private static int totalDeleted = 0;
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        String configPath = System.getenv("CLEANUP_CONFIG_DIR");
        String configFilePath = configPath + "/" + "client_id_client_secret_2023-05-27.json";

        File initialFile = new File(configFilePath);
        InputStream in = Files.newInputStream(initialFile.toPath());
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        List<String> SCOPES = new ArrayList<String>();
        SCOPES.add(GmailScopes.MAIL_GOOGLE_COM);
        SCOPES.add(GmailScopes.GMAIL_MODIFY);
        SCOPES.add(GmailScopes.GMAIL_READONLY);

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    public static void listAndDeleteMessages(Gmail service, String emailId) throws IOException {
        String user = "me";
        ListMessagesResponse messages = service.users().messages().list(user).setQ("from:"+emailId).execute();
        if (messages.getResultSizeEstimate() == 0){
            System.out.println(emailId + ": No message exist");
        } else {
            String nextPageToken = "";
            int currentDeleted = 0;
            while(nextPageToken != null){
                nextPageToken = messages.getNextPageToken();
                for (int i=0; i<messages.getMessages().size(); i++){
                    String messageId = messages.getMessages().get(i).getId();
                    service.users().messages().delete(user, messageId).execute();
                    totalDeleted++;
                    currentDeleted++;
                }
                messages = service.users().messages().list(user).setPageToken(nextPageToken).setQ("from:"+emailId).execute();
            }
            System.out.println(emailId + ": Deleted " + currentDeleted + (currentDeleted > 1 ? " messages" : " message" ));
        }
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Scanner myReader = new Scanner(new File("email_id.txt"));
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            listAndDeleteMessages(service, data);
        }
        System.out.println("\nTotal deleted messaged count: " + totalDeleted);
    }
}
