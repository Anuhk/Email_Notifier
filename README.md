# Email_Notifier
Overview
The Gmail Alert App is an Android application designed to monitor incoming Gmail messages from specific email addresses and trigger a custom notification sound (ringtone) when an email is received. Users can add or remove monitored emails, choose a ringtone, and the app will periodically check for new emails in the background.
________________________________________
Features
1.	Google Sign-In Integration
2.	Encrypted email address storage (AES256)
3.	Custom ringtone picker and preview
4.	Background email polling using WorkManager
5.	UI built using XML with Material-compliant styling
6.	Dark mode support
7.	Email list management with long-press delete
________________________________________
App Flow
1. Login and Permissions
•	User logs in via Google Sign-In.
•	The app requests Gmail API access with gmail.readonly scope.
2. MainActivity.java
•	Displays UI for input and management.
•	Key components:
o	EditText to enter email address
o	ListView for displaying monitored emails
o	Buttons for adding email, selecting ringtone, and manually triggering a check
3. Background Work: EmailPollWorker.java
•	Fetches unread emails using Gmail API.
•	Matches sender against stored monitored email list.
•	Triggers ringtone if match is found.
•	Uses WorkManager for both manual and periodic checking.
4. Secure Storage
•	Uses AndroidX Security EncryptedSharedPreferences
•	Saves:
o	emails: Set
o	ringtone_uri: String
________________________________________
Key Files
1. MainActivity.java
Handles all UI interactions, manages data storage, and triggers background work.
2. EmailPollWorker.java
Runs in the background to access Gmail using OAuth2 token and matches senders.
3. activity_main.xml
Defines the layout with scrollable form, editable fields, and styled components.
4. AndroidManifest.xml
Declares permissions and worker registration.
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
________________________________________

Important Dependencies
•	Google Sign-In SDK
•	Google Play Services Auth
•	WorkManager
•	AndroidX Security Crypto
•	Gmail API (com.google.api-client)

________________________________________
Gmail API Setup
•	Enable Gmail API in Google Cloud Console
•	Configure OAuth 2.0 Client ID for Android
•	Add SHA-1 certificate fingerprint
________________________________________
Deployment Checklist
•	Google Sign-In working with real Gmail
•	Gmail API enabled in GCP Console
•	PeriodicWorkRequest set to 15 mins
•	Runtime permissions handled (if needed)
•	Ringtone plays only once per new email
•	Play Store graphics, description, and privacy policy ready
________________________________________
Testing Tips
•	Add your own Gmail as monitored sender
•	Send a test email and check logs using Logcat
•	Verify token access, ringtone playing, and background execution
________________________________________
Future Improvements
•	Firebase Crashlytics integration
•	Notification UI with sender preview
•	Multi-account support
•	Retry logic for offline mode
________________________________________
Gmail Alert App – Get Instant Alerts for Important Emails!
Stay notified the moment important emails hit your inbox!
Gmail Alert App lets you choose specific sender email addresses and get a custom ringtone alert whenever a new mail arrives from them.
________________________________________
🔔 Key Features:
✅ Real-time Alerts
Get notified instantly (every 15 minutes) for emails from selected senders. Perfect for job updates, client communication, or critical messages.

✅ Custom Ringtone Selection
Choose your own ringtone to make alerts stand out. Know which email is important—without looking at your phone!

✅ Secure Email Storage
Your monitored email addresses are securely encrypted using Android’s EncryptedSharedPreferences.

✅ Google Sign-In Integration
Seamlessly log in using your Gmail account—quick, easy, and secure.

✅ Remove Saved Addresses
Update your monitored senders anytime by long-pressing to delete.

✅ Lightweight and Easy to Use
Minimal UI with simple setup—works quietly in the background.

________________________________________
🛠 How it Works:
1.	Sign in using your Gmail account.
2.	Add the email addresses you want to monitor.
3.	Select your preferred notification ringtone.
4.	That’s it! The app checks your inbox every 15 minutes and alerts you when a new mail arrives from saved addresses.
________________________________________
🔒 Your Privacy Matters
•	The app only uses read-only access to check for unread emails.
•	No data is stored on external servers—everything stays on your device.
________________________________________
📝 Notes:
•	Ensure Gmail API is enabled for the account (done automatically at first login).
•	The app does not read or store the content of your emails.
•	Battery optimization settings may affect background notifications. Consider allowing background activity.
________________________________________
📱 Perfect for:
•	Freelancers and business owners awaiting client responses
•	Job seekers tracking interview emails
•	Students and professionals needing instant alerts
•	Anyone who doesn't want to miss an important mail!
________________________________________
Download Now & Never Miss an Important Email Again!

