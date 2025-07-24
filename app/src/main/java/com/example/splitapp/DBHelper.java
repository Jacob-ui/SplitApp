package com.example.splitapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "splitmoney.db"; // Name der Datenbank
    // Versionsnummer entfernt

    // Konstruktor
    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1); // Version fest auf 1
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE User (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");
        db.execSQL("CREATE TABLE GroupTable (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, password TEXT)");
        db.execSQL("CREATE TABLE GroupMember (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, group_id INTEGER)");
        db.execSQL("CREATE TABLE MoneyIssue (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, sender_id INTEGER, recipient_id INTEGER, amount REAL, group_id INTEGER)");
    }

    // Wird aufgerufen, wenn sich die Datenbankversion ändert
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Alle Tabellen löschen und neu erstellen
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS GroupTable");
        db.execSQL("DROP TABLE IF EXISTS GroupMember");
        db.execSQL("DROP TABLE IF EXISTS MoneyIssue");
        onCreate(db);
    }

    // Neuen Benutzer einfügen
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert("User", null, values);
        return result != -1;
    }

    // Überprüfen, ob ein Benutzer mit Username und Passwort existiert
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM User WHERE username=? AND password=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }


    // Neue Gruppe einfügen
    public long insertGroup(String name, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("password", password);
        return db.insert("GroupTable", null, values);
    }

    // ID einer Gruppe anhand von Name und Passwort abrufen
    public int getGroupId(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM GroupTable WHERE name=? AND password=?", new String[]{name, password});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        } else {
            cursor.close();
            return -1; // Gruppe nicht gefunden
        }
    }


    // Neues Gruppenmitglied einfügen
    public boolean insertMember(String name, int groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("group_id", groupId);
        long result = db.insert("GroupMember", null, values);
        return result != -1;
    }

    // Liste aller Mitgliedsnamen einer bestimmten Gruppe abrufen
    public List<String> getMemberNames(int groupId) {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Abfrage der Mitgliedsnamen aus der richtigen Tabelle
        Cursor cursor = db.rawQuery("SELECT name FROM GroupMember WHERE group_id = ?", new String[]{String.valueOf(groupId)});
        if (cursor.moveToFirst()) {
            do {
                names.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return names;
    }

    // Map mit Namen → Mitglieds-ID für eine Gruppe
    public Map<String, Integer> getMemberIdMap(int groupId) {
        Map<String, Integer> map = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name FROM GroupMember WHERE group_id=?", new String[]{String.valueOf(groupId)});
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            String name = c.getString(c.getColumnIndexOrThrow("name"));
            map.put(name, id);
        }
        c.close();
        return map;
    }

    // Namen eines Mitglieds anhand seiner ID abrufen
    public String getMemberName(int memberId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM GroupMember WHERE id=?", new String[]{String.valueOf(memberId)});
        String name = "";
        if (c.moveToFirst()) {
            name = c.getString(0);
        }
        c.close();
        return name;
    }

    // Neue Transaktion einfügen
    public boolean insertTransaction(String title, int senderId, int recipientId, float amount, int groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("sender_id", senderId);
        values.put("recipient_id", recipientId);
        values.put("amount", amount);
        values.put("group_id", groupId);
        long result = db.insert("MoneyIssue", null, values);
        return result != -1;
    }

    // Alle Transaktionen eines Mitglieds in einer Gruppe abrufen (als Sender oder Empfänger)
    public List<MoneyIssue> getMoneyIssuesByMember(int groupId, int memberId) {
        List<MoneyIssue> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM MoneyIssue WHERE group_id=? AND (sender_id=? OR recipient_id=?)",
                new String[]{String.valueOf(groupId), String.valueOf(memberId), String.valueOf(memberId)});
        while (c.moveToNext()) {
            MoneyIssue issue = new MoneyIssue();
            issue.id = c.getInt(c.getColumnIndexOrThrow("id"));
            issue.title = c.getString(c.getColumnIndexOrThrow("title"));
            issue.senderId = c.getInt(c.getColumnIndexOrThrow("sender_id"));
            issue.recipientId = c.getInt(c.getColumnIndexOrThrow("recipient_id"));
            issue.amount = c.getFloat(c.getColumnIndexOrThrow("amount"));
            list.add(issue);
        }
        c.close();
        return list;
    }

    // Eine Transaktion löschen
    public void deleteMoneyIssue(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("MoneyIssue", "id=?", new String[]{String.valueOf(id)});
    }

    public static class MoneyIssue {
        public int id;// ID der Transaktion
        public String title;// Beschreibung/Titel
        public int senderId;// Wer hat gezahlt
        public int recipientId;// Wer hat das Geld erhalten
        public float amount;// Betrag
    }
}
