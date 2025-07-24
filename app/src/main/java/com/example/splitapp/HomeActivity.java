package com.example.splitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    // Buttons für verschiedene Aktionen
    Button btnAddMember, btnNewMoneyIssue, btnGroupOverview, btnLogout;

    // Die ID der aktuell ausgewählten Gruppe
    int groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // ruft grundlegende Activity-Logik auf
        setContentView(R.layout.activity_home); // zeigt die Oberfläche der HomeActivity

        // Verknüpft Buttons mit den Elementen im Layout
        btnAddMember = findViewById(R.id.btnAddMember);
        btnNewMoneyIssue = findViewById(R.id.btnNewMoneyIssue);
        btnGroupOverview = findViewById(R.id.btnGroupOverview);
        btnLogout = findViewById(R.id.btnLogout); // Logout-Button

        // Holt die übergebene Gruppen-ID (z.B. von Login oder Gruppen-Auswahl)
        groupId = getIntent().getIntExtra("group_id", -1); // -1 = Standardwert, falls nichts mitgegeben wurde

        // ► Button: "Mitglied hinzufügen"
        btnAddMember.setOnClickListener(v -> {
            // Starte AddMemberActivity und übergebe groupId
            Intent intent = new Intent(HomeActivity.this, AddMemberActivity.class);
            intent.putExtra("group_id", groupId);
            startActivity(intent);
        });

        // ► Button: "Ausgabe hinzufügen"
        btnNewMoneyIssue.setOnClickListener(v -> {
            // Starte NewMoneyIssueActivity und übergebe groupId
            Intent intent = new Intent(HomeActivity.this, NewMoneyIssueActivity.class);
            intent.putExtra("group_id", groupId);
            startActivity(intent);
        });

        // ► Button: "Gruppenübersicht"
        btnGroupOverview.setOnClickListener(v -> {
            // Starte SplitMoneyActivity und übergebe groupId
            Intent intent = new Intent(HomeActivity.this, SplitMoneyActivity.class);
            intent.putExtra("group_id", groupId);
            startActivity(intent);
        });

        // ► Button: "Logout"
        btnLogout.setOnClickListener(v -> {
            // Löscht gespeicherte Login-Daten aus SharedPreferences
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Geht zurück zur LoginActivity und entfernt alle offenen Activities
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Beendet die aktuelle HomeActivity
        });
    }
}
