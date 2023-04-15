package CS360.nicksinventoryapp;


import android.widget.CompoundButton;
import android.content.DialogInterface;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;


import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    public static String NOTIFICATIONS_PREFERENCE = "notify_pref";
    private final int SMS_CODE = 69;
    SwitchMaterial notifyToggle;
    SharedPreferences sharedPrefs;
    boolean notifications = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the slide-in and slide-out animation between activities
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        // Set the layout file for this activity
        setContentView(R.layout.settings_activity);

        // Find the toggle switch view by its ID
        notifyToggle = findViewById(R.id.notifications_toggle);

        // Listener for toggle switch
        notifyToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the notifications preference based on the toggle switch state
                notifications = isChecked;
                // Check for permissions
                if (isChecked && hasPermissions()) {
                    Log.d(TAG, "SMS notifications are on");
                    notifyToggle.setChecked(true);
                }
                else {
                    Log.d(TAG, "SMS notifications are off");
                    notifyToggle.setChecked(false);
                    notifications = false;
                }

                // Update the user's preferences for this app
                savePreferences();
            }
        });

        // Access the default shared prefs
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the user's notification preference from shared preferences
        notifications = sharedPrefs.getBoolean(NOTIFICATIONS_PREFERENCE, false);

        // Set the initial state of the toggle switch based on the permissions and preferences selected
        if (notifications
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED
        ) {
            notifyToggle.setChecked(true);
        }
    }



    // Checks if the user has granted permission for SMS notifications.
    // Prompts the user to grant permission if it has not been granted already.
    private boolean hasPermissions() {
        String smsPermission = Manifest.permission.SEND_SMS;

        // If app does not have permission for SMS notifications
        if (ContextCompat.checkSelfPermission(this, smsPermission) != PackageManager.PERMISSION_GRANTED) {

            // Check if user has previously denied the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, smsPermission)) {
                // Show explanation to the user
                new AlertDialog.Builder(this)
                        .setTitle(R.string.SMS_permission_needed)
                        .setMessage(R.string.notification_description)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Request user's permission
                                ActivityCompat.requestPermissions(
                                        SettingsActivity.this,
                                        new String[]{smsPermission},
                                        SMS_CODE
                                );
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                // Request permissions from the user
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{smsPermission},
                        SMS_CODE
                );
            }
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SMS_CODE: {
                // Check if permission for sending SMS notifications is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If permission is granted, enable notifications
                    Log.d(TAG, "Permission granted");
                    notifications = true;
                    notifyToggle.setChecked(true);
                } else {
                    // If permission is denied, disable notifications
                    Log.d(TAG, "Permission denied");
                    notifications = false;
                    notifyToggle.setChecked(false);
                }

                // Save notification preferences
                savePreferences();
            }
        }
    }


    /**
     * Saves the current notifications preference to shared preferences.
     */
    private void savePreferences() {
        // Get a reference to the shared preferences editor
        SharedPreferences.Editor editor = sharedPrefs.edit();

        // Add the notifications preference to the editor
        editor.putBoolean(NOTIFICATIONS_PREFERENCE, notifications);

        // Apply the changes to the shared preferences
        editor.apply();
    }


}
