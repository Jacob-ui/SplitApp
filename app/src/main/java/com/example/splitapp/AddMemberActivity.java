package com.example.splitapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddMemberActivity extends AppCompatActivity {

    // UI-Elemente
    EditText etMemberName;
    Button btnAddMember, btnContinue;
    int groupId; // ID der Gruppe, zu der Mitglieder hinzugefügt werden

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member); // Layout setzen

        // UI-Elemente mit Layout-Elementen verbinden
        etMemberName = findViewById(R.id.etMemberName);
        btnAddMember = findViewById(R.id.btnAddMember);
        btnContinue = findViewById(R.id.btnContinue);

        // Datenbank-Helfer initialisieren
        DBHelper dbHelper = new DBHelper(this);

        // Group-ID aus vorheriger Activity übernehmen
        groupId = getIntent().getIntExtra("group_id", -1);

        // Listener für "Mitglied hinzufügen"-Button
        btnAddMember.setOnClickListener(v -> {
            String name = etMemberName.getText().toString(); // Eingabe lesen

            if (!name.isEmpty()) {
                // Wenn Name nicht leer ist, Mitglied zur DB hinzufügen
                if (dbHelper.insertMember(name, groupId)) {
                    Toast.makeText(this, "Mitglied hinzugefügt", Toast.LENGTH_SHORT).show();
                    etMemberName.setText(""); // Eingabefeld leeren
                } else {
                    Toast.makeText(this, "Fehler beim Hinzufügen", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Hinweis bei leerem Namen
                Toast.makeText(this, "Bitte Namen eingeben", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener für "Weiter"-Button
        btnContinue.setOnClickListener(v -> {
            // Wechsel zur HomeActivity und Übergabe der Gruppen-ID
            Intent intent = new Intent(AddMemberActivity.this, HomeActivity.class);
            intent.putExtra("group_id", groupId);
            startActivity(intent);
            finish(); // aktuelle Activity schließen
        });
    }
}
