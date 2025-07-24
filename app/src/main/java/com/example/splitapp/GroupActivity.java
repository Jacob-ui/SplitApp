package com.example.splitapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GroupActivity extends AppCompatActivity {

    // Eingabefelder für Gruppennamen und Passwort
    EditText etGroupName, etGroupPassword;

    // Buttons für Gruppenaktionen
    Button btnCreateGroup, btnJoinGroup;

    // Datenbank-Hilfe für Zugriff auf Gruppen
    DBHelper dbHelper;

    // ID der aktuell ausgewählten oder neu erstellten Gruppe
    int currentGroupId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Initialisiert die Activity
        setContentView(R.layout.activity_group); // Zeigt das Gruppen-Layout an

        // Verknüpft UI-Elemente aus dem Layout mit Java-Variablen
        etGroupName = findViewById(R.id.etGroupName);
        etGroupPassword = findViewById(R.id.etGroupPassword);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);

        // Initialisiert die Datenbankverbindung
        dbHelper = new DBHelper(this);

        // ► Button: Neue Gruppe erstellen
        btnCreateGroup.setOnClickListener(v -> {
            String name = etGroupName.getText().toString().trim();       // Gruppennamen lesen
            String password = etGroupPassword.getText().toString().trim(); // Passwort lesen

            // Eingaben prüfen
            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte Gruppennamen und Passwort eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            // Versucht, eine neue Gruppe in der Datenbank zu speichern
            long groupId = dbHelper.insertGroup(name, password);
            if (groupId != -1) {
                currentGroupId = (int) groupId; // Speichert ID der neuen Gruppe
                goToAddMembers(currentGroupId); // Wechselt zur Mitgliederseite
            } else {
                Toast.makeText(this, "Gruppe konnte nicht erstellt werden", Toast.LENGTH_SHORT).show();
            }
        });

        // ► Button: Bestehender Gruppe beitreten
        btnJoinGroup.setOnClickListener(v -> {
            String name = etGroupName.getText().toString().trim();
            String password = etGroupPassword.getText().toString().trim();

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte Gruppennamen und Passwort eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            // Überprüft, ob die Gruppe existiert (z.B. für den Login)
            int groupId = dbHelper.getGroupId(name, password);
            if (groupId != -1) {
                currentGroupId = groupId;
                goToHome(currentGroupId); // Wechselt zur Startseite der Gruppe
            } else {
                Toast.makeText(this, "Gruppe nicht gefunden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Methode zum Wechsel zur "Mitglieder hinzufügen"-Seite
    private void goToAddMembers(int groupId) {
        Intent intent = new Intent(GroupActivity.this, AddMemberActivity.class);
        intent.putExtra("group_id", groupId); // Übergibt die Gruppen-ID an die nächste Activity
        startActivity(intent);
        finish(); // Beendet diese Activity, damit man nicht mit "Zurück" wieder hier landet
    }

    // Methode zum Wechsel zur HomeActivity (Startseite der Gruppe)
    private void goToHome(int groupId) {
        Intent intent = new Intent(GroupActivity.this, HomeActivity.class);
        intent.putExtra("group_id", groupId);
        startActivity(intent);
        finish();
    }
}
