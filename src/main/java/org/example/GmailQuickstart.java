package org.example;

import com.google.api.client.auth.oauth2.Credential;
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
import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/* class to demonstrate use of Gmail list labels API */
public class GmailQuickstart {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    //private static final List<String> SCOPES = new ArrayList<>(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_MODIFY);
    //Collections.singletonList(GmailScopes.GMAIL_LABELS);
    private static final String CREDENTIALS_FILE_PATH = "/client_id_client_secret_2023-05-27.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        List<String> SCOPES = new ArrayList<>();
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
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static int listAndDeleteMessages(Gmail service, String emailId) throws IOException {
        int totalDeletedMessages = 0;
        String user = "me";
        ListMessagesResponse messages = service.users().messages().list(user).setQ("from:"+emailId).execute();
        if (messages.getResultSizeEstimate() == 0){
            System.out.println(emailId + ": No message exist");
        } else {
            int messageCount = messages.getMessages().size();
            for (int i=0; i<messageCount; i++){
                String messageId = messages.getMessages().get(i).getId();
                service.users().messages().delete(user, messageId).execute();
                totalDeletedMessages++;
            }
            System.out.println(emailId + ": Deleted " + messageCount + (messageCount>1?" messages": " message"));
        }
        return totalDeletedMessages;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Scanner myReader = new Scanner(new File("email_id.txt"));
        int deletedMessageCount = 0;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            deletedMessageCount += listAndDeleteMessages(service, data);
        }

        System.out.println("Total deleted messages: " + deletedMessageCount);

        //listAndDeleteMessages(service, "oldnavy@email.oldnavy.ca");

    }
}
