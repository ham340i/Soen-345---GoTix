package com.example.gotix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gotix.model.User;
import com.example.gotix.store.UserStore;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private RadioGroup roleGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.reg_et_username);
        etPassword = findViewById(R.id.reg_et_password);
        roleGroup = findViewById(R.id.reg_role_group);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegistration() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedId = roleGroup.getCheckedRadioButtonId();
        
        String role = (selectedId == R.id.reg_radio_admin) ? "admin" : "end_user";

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (UserStore.exists(username, role)) {
            Toast.makeText(this, "User already exists for this role", Toast.LENGTH_SHORT).show();
        } else {
            User newUser = new User(UUID.randomUUID().toString(), username, password, role, System.currentTimeMillis());
            UserStore.addUser(newUser);
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
