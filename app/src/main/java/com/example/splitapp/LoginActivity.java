package com.example.splitapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitapp.DBHelper;

public class LoginActivity extends AppCompatActivity {

    // Eingabefelder für Benutzername und Passwort
    EditText etUsername, etPassword;

    // Buttons für Registrierung und Login
    Button btnRegister, btnLogin;

    // Zugriff auf die Datenbank
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Startet die Activity mit Systemfunktionen
        setContentView(R.layout.activity_login); // Zeigt das Layout der Login-Seite an

        // Verbindet die XML-Elemente mit Java-Variablen
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        // Erstellt eine Instanz der Datenbank-Hilfe
        dbHelper = new DBHelper(this);

        // Was passiert, wenn auf "Registrieren" geklickt wird
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString(); // Eingabe auslesen
            String password = etPassword.getText().toString();

            if (dbHelper.insertUser(username, password)) {
                // Registrierung erfolgreich
                Toast.makeText(this, "Registrierung erfolgreich", Toast.LENGTH_SHORT).show();
                goToGroupScreen(); // Wechsel zur Gruppenübersicht
            } else {
                // Benutzer existiert bereits
                Toast.makeText(this, "Benutzer existiert bereits", Toast.LENGTH_SHORT).show();
            }
        });

        // Was passiert, wenn auf "Login" geklickt wird
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString(); // Eingabe auslesen
            String password = etPassword.getText().toString();

            if (dbHelper.checkUser(username, password)) {
                // Login erfolgreich
                Toast.makeText(this, "Login erfolgreich", Toast.LENGTH_SHORT).show();
                goToGroupScreen(); // Wechsel zur Gruppenübersicht
            } else {
                // Zugangsdaten falsch
                Toast.makeText(this, "Falsche Zugangsdaten", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Methode zum Wechsel zur Gruppenansicht
    private void goToGroupScreen() {
        // Mit einem Intent wird die neue Activity gestartet
        Intent intent = new Intent(LoginActivity.this, GroupActivity.class);
        startActivity(intent); // Starte Gruppenansicht
        finish(); // Beende LoginActivity (damit man mit "Zurück" nicht wieder hier landet)
    }
}
