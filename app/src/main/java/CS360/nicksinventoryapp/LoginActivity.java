package CS360.nicksinventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    DatabaseHelper DatabaseHelper;

    Button loginButton;
    Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the singleton instance of the app database
        DatabaseHelper = DatabaseHelper.getInstance(this);

        usernameEditText = findViewById(R.id.username_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.create_account_button);

        // Disable buttons by default
        loginButton.setEnabled(false);
        createAccountButton.setEnabled(false);

        // Get changes in fields
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
    }

    // Define a TextWatcher object that monitors changes to the text fields
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing before text is changed
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // When text is changed, check if username and password fields are empty
            boolean fieldsAreEmpty = getUsername().isEmpty() || getPassword().isEmpty();

            // Enable or disable the login and create account buttons based on whether fields are empty
            loginButton.setEnabled(!fieldsAreEmpty);
            createAccountButton.setEnabled(!fieldsAreEmpty);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Do nothing after text is changed
        }
    };

    // Login button functionality
    public void login(View view) {
        // Validate user login credentials or send error
        if (!validCredentials()) {
            showError(view.getContext().getResources().getString(R.string.invalid_login));
            return;
        }

        try {
            // log in the user with credentials
            boolean isLoggedIn = DatabaseHelper.validateUser(getUsername(), hash(getPassword()));

            // Go to Inventory for user
            if (isLoggedIn) {
                handleLoggedInUser();
            }
            // show error if invalid login
            else {
                showError(view.getContext().getResources().getString(R.string.invalid_login));
            }
        }
        catch (Exception e) {
            showError(view.getContext().getResources().getString(R.string.invalid_login));
        }
    }

    // Create Account button functionality
    public void createAccount(View view) {
        // Validate user account credentials or send error
        if (!validCredentials()) {
            showError(view.getContext().getResources().getString(R.string.registration_error));
        }

        try {
            // Create new user
            boolean userCreated = DatabaseHelper.createUser(getUsername(), hash(getPassword()));

            // Go to inventory for user.
            if (userCreated) {
                handleLoggedInUser();
            }
            // show error if invalid login
            else {
                showError(view.getContext().getResources().getString(R.string.registration_error));
            }
        }
        catch (Exception e) {
            showError(view.getContext().getResources().getString(R.string.registration_error));
        }
    }

    // Go to Inventory Activity
    private void handleLoggedInUser() {
        Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
        startActivity(intent);
    }

    // Validate user credentials
    private boolean validCredentials() {
        return !getUsername().isEmpty() && !getPassword().isEmpty();
    }

    // get the username from the edittext view
    private String getUsername() {
        Editable username = usernameEditText.getText();
        return username != null ? username.toString().trim().toLowerCase() : "";
    }

    // get the user password from the edittext view
    private String getPassword() {
        Editable password = passwordEditText.getText();
        return password != null ? password.toString().trim() : "";
    }

    /**
     * Computes the MD5 hash of the given password string and returns the result as a hexadecimal string.
     *
     * @param password the password string to hash
     * @return the MD5 hash of the password as a hexadecimal string
     * @throws Exception if an error occurs during the hashing process
     */
    private String hash(String password) throws Exception {
        // Create a new MD5 message digest instance
        MessageDigest md = MessageDigest.getInstance("MD5");

        // Update the message digest with the bytes of the password string
        md.update(password.getBytes());

        // Compute the digest and get the bytes as an array
        byte[] digest = md.digest();

        // Convert the digest bytes to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }

        // Return the resulting hexadecimal string
        return sb.toString();
    }

    /**
     * Displays an error message to the user using a toast notification.
     *
     * @param errorMessage the error message to display
     */
    private void showError(String errorMessage) {
        // Create a new toast notification with the error message
        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);

        // Set the toast's position to be centered horizontally and 200 pixels above the center of the screen vertically
        toast.setGravity(Gravity.CENTER, 0, -200);

        // Show the toast notification
        toast.show();
    }
}