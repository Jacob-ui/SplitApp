package com.example.splitapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewMoneyIssueActivity extends AppCompatActivity {

    // UI-Komponenten
    EditText etTitle, etAmount;               // Eingabefelder für Titel und Betrag
    Spinner spinnerSender;                    // Dropdown zur Auswahl des Zahlers
    LinearLayout layoutRecipients;            // Layout für Empfänger-Checkboxen
    Button btnFinish, btnBack;                // Buttons zum Speichern oder Zurückkehren

    DBHelper dbHelper;                        // Datenbankhelfer
    int groupId;                              // Gruppen-ID
    List<String> memberNames;                 // Liste der Mitgliedsnamen
    Map<String, Integer> memberIdMap;         // Zuordnung: Name → ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_money_issue);

        // UI-Elemente mit Layout-Elementen verknüpfen
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        spinnerSender = findViewById(R.id.spinnerSender);
        layoutRecipients = findViewById(R.id.layoutRecipients);
        btnFinish = findViewById(R.id.btnFinish);
        btnBack = findViewById(R.id.btnBack);

        // Datenbankhelfer initialisieren und Gruppen-ID abrufen
        dbHelper = new DBHelper(this);
        groupId = getIntent().getIntExtra("group_id", -1);

        // Mitgliedsdaten aus der Datenbank laden
        memberNames = dbHelper.getMemberNames(groupId);// Namen zur Anzeige
        memberIdMap = dbHelper.getMemberIdMap(groupId);// Namen mit ID z.B. für Transaktion

        // Spinner mit Mitgliedsnamen befüllen
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberNames);
        spinnerSender.setAdapter(adapter);

        // Dynamisch Checkboxen für jeden Empfänger hinzufügen
        for (String name : memberNames) {
            CheckBox cb = new CheckBox(this);
            cb.setText(name);
            layoutRecipients.addView(cb);
        }

        // Klicklistener für den Speichern-Button
        btnFinish.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim(); // Titel auslesen
            String amountStr = etAmount.getText().toString().trim(); // Betrag als String

            // Prüfen, ob Titel und Betrag eingegeben wurden
            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Bitte Betreff und Betrag eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            float amount = Float.parseFloat(amountStr); // Betrag in Float umwandeln
            String senderName = spinnerSender.getSelectedItem().toString(); // Name des Zahlers
            int senderId = memberIdMap.get(senderName); // ID des Zahlers

            // Liste erstellen, um die IDs der ausgewählten Empfänger zu speichern
            List<Integer> recipientIds = new ArrayList<>();// Alle Namen (CheckBoxes) im Layout durchgehen
            for (int i = 0; i < layoutRecipients.getChildCount(); i++) { // Das i-te holen und in eine CheckBox umwandeln
                CheckBox cb = (CheckBox) layoutRecipients.getChildAt(i);// Prüfen, ob die CheckBox aktiviert (angeklickt) ist
                if (cb.isChecked()) {// Den Namen aus dem Text der CheckBox holen (z.B. "Anna")
                    String memberName = cb.getText().toString();// Die zugehörige Mitglieds-ID aus der Map abrufen
                    int recipientId = memberIdMap.get(memberName);// Die ID der Liste der ausgewählten Empfänger hinzufügen
                    recipientIds.add(recipientId);
                }
            }
            // Prüfen, ob Empfänger ausgewählt wurden
            if (recipientIds.isEmpty()) {
                Toast.makeText(this, "Bitte Empfänger auswählen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Betrag durch Anzahl der Empfänger teilen
            float individualAmount = amount / recipientIds.size();

            // Für jeden Empfänger eine Transaktion einfügen
            for (int recipientId : recipientIds) {
                dbHelper.insertTransaction(title, senderId, recipientId, individualAmount, groupId);
            }

            Toast.makeText(this, "Transaktionen gespeichert", Toast.LENGTH_SHORT).show();
            finish(); //Activity beenden → zurück zur vorherigen Seite
        });

        // Klicklistener für den Zurück-Button
        btnBack.setOnClickListener(v -> finish()); //Einfach Activity schließen
    }
}
