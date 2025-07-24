package com.example.splitapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.splitapp.DBHelper.MoneyIssue;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SplitMoneyActivity extends AppCompatActivity {

    Spinner spinnerFilterMember;            // Dropdown zur Auswahl eines Mitglieds
    TextView tvSaldo;                       // Anzeige des Gesamtsaldos
    LinearLayout layoutTransactions;        // Container für alle Transaktionen

    DBHelper dbHelper;
    int groupId;                            // ID der aktuellen Gruppe
    Map<String, Integer> memberIdMap;       // Name → ID Mapping der Mitglieder
    List<String> memberNames;               // Liste aller Mitgliedsnamen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_money);

        // UI-Elemente initialisieren
        spinnerFilterMember = findViewById(R.id.spinnerFilterMember);
        tvSaldo = findViewById(R.id.tvSaldo);
        layoutTransactions = findViewById(R.id.layoutTransactions);

        // Zurück-Button schließt die Aktivität
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        dbHelper = new DBHelper(this);
        groupId = getIntent().getIntExtra("group_id", -1); // Gruppen-ID vom vorherigen Intent

        // Mitgliederinformationen aus der Datenbank abrufen
        memberNames = dbHelper.getMemberNames(groupId);
        memberIdMap = dbHelper.getMemberIdMap(groupId);

        // Spinner mit Mitgliedsnamen befüllen
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberNames);
        spinnerFilterMember.setAdapter(adapter);

        // Ereignis: Auswahl eines Mitglieds im Dropdown
        spinnerFilterMember.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedName = memberNames.get(pos);
                int selectedId = memberIdMap.get(selectedName);
                showTransactions(selectedId); // Transaktionen für das gewählte Mitglied anzeigen
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Wenn Mitglieder vorhanden sind, erstes auswählen
        if (!memberNames.isEmpty()) {
            spinnerFilterMember.setSelection(0);
        }
    }

    /**
     * Zeigt alle Transaktionen für ein bestimmtes Mitglied an
     */
    private void showTransactions(int memberId) {
        layoutTransactions.removeAllViews(); // Vorherige Transaktionen entfernen
        float saldo = 0f;

        // Transaktionen dieses Mitglieds laden
        List<MoneyIssue> issues = dbHelper.getMoneyIssuesByMember(groupId, memberId);

        for (MoneyIssue issue : issues) {
            String label = issue.title + ": "; // Titel der Transaktion
            String amountStr = String.format(Locale.GERMANY, "%.2f", issue.amount); // Formatierter Betrag

            CheckBox checkBox = new CheckBox(this);
            checkBox.setTextSize(16);
            checkBox.setPadding(10, 10, 10, 10);

            // Prüfen, ob das Mitglied der Sender ist (also bezahlt hat)
            if (issue.senderId == memberId) {
                label += "+ " + amountStr + " € von " + dbHelper.getMemberName(issue.recipientId);
                saldo -= issue.amount; // Geld wurde gegeben → verringert Saldo
                checkBox.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // grün
            } else {
                // Mitglied hat Geld erhalten
                label += "- " + amountStr + " € an " + dbHelper.getMemberName(issue.senderId);
                saldo += issue.amount; // Geld empfangen → erhöht Schulden
                checkBox.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // rot
            }

            checkBox.setText(label);

            // Wenn Checkbox aktiviert wird → Transaktion löschen
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    dbHelper.deleteMoneyIssue(issue.id); // Aus Datenbank entfernen
                    showTransactions(memberId);          // Anzeige aktualisieren
                }
            });

            layoutTransactions.addView(checkBox); // Checkbox zur Liste hinzufügen
        }

        // Saldo invertieren zur besseren Anzeige
        float displaySaldo = -saldo;
        String saldoText = "Saldo: " + String.format(Locale.GERMANY, "%+.2f €", displaySaldo);
        tvSaldo.setText(saldoText);

        // Farbliche Darstellung des Saldos: grün = bekommt Geld, rot = schuldet
        if (saldo > 0) {
            tvSaldo.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // schuldet
        } else {
            tvSaldo.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // bekommt
        }
    }
}
