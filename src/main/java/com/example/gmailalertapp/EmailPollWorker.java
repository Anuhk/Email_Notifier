package com.example.gmailalertapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmailPollWorker extends Worker {

    public EmailPollWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                Log.e("EmailPollWorker", "No signed-in account");
                return Result.failure();
            }

            String token = GoogleAuthUtil.getToken(context, account.getAccount(),
                    "oauth2:https://www.googleapis.com/auth/gmail.readonly");

            Gmail service = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new GoogleCredential().setAccessToken(token)
            ).setApplicationName("Gmail Alert App").build();

            // Get unread messages
            ListMessagesResponse response = service.users().messages()
                    .list("me")
                    .setQ("is:unread")
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                Log.d("EmailPollWorker", "No unread messages");
                return Result.success();
            }

            // Load saved emails and ringtone from encrypted prefs
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            Set<String> savedEmails = prefs.getStringSet("emails", new HashSet<>());
            String ringtoneUri = prefs.getString("ringtone_uri", null);
            String lastAlertedId = prefs.getString("last_alerted_email_id", null);

            for (Message msg : messages) {
                String messageId = msg.getId();
                if (messageId.equals(lastAlertedId)) {
                    Log.d("EmailPollWorker", "Skipping already alerted message: " + messageId);
                    continue;
                }

                Message fullMessage = service.users().messages()
                        .get("me", messageId)
                        .setFormat("metadata")
                        .setMetadataHeaders(List.of("From"))
                        .execute();

                String from = extractSender(fullMessage);
                Log.d("EmailPollWorker", "Message from: " + from);

                if (savedEmails.contains(from)) {
                    Log.d("EmailPollWorker", "Match found. Playing ringtone.");
                    playRingtone(context, ringtoneUri);

                    prefs.edit().putString("last_alerted_email_id", messageId).apply();
                    break; // only ring for one new matching email
                }
            }

        } catch (Exception e) {
            Log.e("EmailPollWorker", "Error polling email", e);
            return Result.failure();
        }

        return Result.success();
    }

    private String extractSender(Message message) {
        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if ("From".equalsIgnoreCase(header.getName())) {
                String value = header.getValue();
                if (value.contains("<") && value.contains(">")) {
                    return value.substring(value.indexOf('<') + 1, value.indexOf('>')).trim();
                } else {
                    return value.trim();
                }
            }
        }
        return "";
    }

    private void playRingtone(Context context, String uriStr) {
        try {
            if (uriStr == null) {
                Log.e("EmailPollWorker", "No ringtone URI set");
                return;
            }

            Uri uri = Uri.parse(uriStr);
            Ringtone ringtone = RingtoneManager.getRingtone(context, uri);

            if (ringtone != null && !ringtone.isPlaying()) {
                ringtone.play();
                Log.d("EmailPollWorker", "Ringtone played");
            } else {
                Log.e("EmailPollWorker", "Ringtone is null or already playing");
            }
        } catch (Exception e) {
            Log.e("EmailPollWorker", "Failed to play ringtone", e);
        }
    }
}
