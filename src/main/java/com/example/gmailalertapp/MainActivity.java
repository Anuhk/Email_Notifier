package com.example.gmailalertapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.work.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText emailInput;
    Button addEmailButton, selectRingtoneButton, checkNowButton;
    TextView ringtoneLabel;
    ListView emailListView;
    ArrayAdapter<String> adapter;
    ArrayList<String> emailList = new ArrayList<>();

    SharedPreferences securePrefs;
    final String PREF_EMAILS = "emails";
    final String PREF_RINGTONE = "ringtone_uri";
    final int RINGTONE_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInput = findViewById(R.id.email_input);
        addEmailButton = findViewById(R.id.add_email_button);
        selectRingtoneButton = findViewById(R.id.select_ringtone_button);
        checkNowButton = findViewById(R.id.check_now_button);
        ringtoneLabel = findViewById(R.id.ringtone_label);
        emailListView = findViewById(R.id.email_list_view);

        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePrefs = EncryptedSharedPreferences.create(
                    this,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Encryption error", Toast.LENGTH_SHORT).show();
        }

        updateEmailList();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emailList);
        emailListView.setAdapter(adapter);

        addEmailButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!emailList.contains(email)) {
                    emailList.add(email);
                    saveEmails();
                    adapter.notifyDataSetChanged();
                    emailInput.setText("");
                    Toast.makeText(this, "Email added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Email already added", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            }
        });

        emailListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String email = emailList.get(i);
            emailList.remove(email);
            saveEmails();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Removed: " + email, Toast.LENGTH_SHORT).show();
            return true;
        });

        selectRingtoneButton.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            startActivityForResult(intent, RINGTONE_REQUEST_CODE);
        });

        checkNowButton.setOnClickListener(v -> {
            WorkManager.getInstance(this).enqueue(
                    new OneTimeWorkRequest.Builder(EmailPollWorker.class).build()
            );
            Toast.makeText(this, "Checking now...", Toast.LENGTH_SHORT).show();
        });

        updateRingtoneLabel();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                EmailPollWorker.class,
                15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "email_check",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest);
    }

    private void saveEmails() {
        Set<String> emails = new HashSet<>(emailList);
        securePrefs.edit().putStringSet(PREF_EMAILS, emails).apply();
    }

    private void updateEmailList() {
        Set<String> emails = securePrefs.getStringSet(PREF_EMAILS, new HashSet<>());
        emailList.clear();
        if (emails != null) {
            emailList.addAll(emails);
        }
    }

    private void updateRingtoneLabel() {
        String uriStr = securePrefs.getString(PREF_RINGTONE, null);
        if (uriStr != null) {
            Uri uri = Uri.parse(uriStr);
            Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
            if (ringtone != null) {
                ringtoneLabel.setText("Selected Ringtone: " + ringtone.getTitle(this));
            } else {
                ringtoneLabel.setText("Selected Ringtone: None");
            }
        } else {
            ringtoneLabel.setText("Selected Ringtone: None");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RINGTONE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                securePrefs.edit().putString(PREF_RINGTONE, uri.toString()).apply();
                updateRingtoneLabel();

                // Preview the ringtone
                Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                if (ringtone != null) {
                    ringtone.play();
                }
            }
        }
    }
}
