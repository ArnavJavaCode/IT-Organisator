package KAGO_framework.control;

import KAGO_framework.Config;
import KAGO_framework.model.abitur.datenbanken.mysql.QueryResult;
import KAGO_framework.model.abitur.datenstrukturen.Queue;

import javax.xml.crypto.Data;
import java.sql.*;

/**
 * Der Database-Controller dient zur Herstellung einer Verbindung mit einem SQL-DBMS und
 * kann SQL-Anfragen bzw. Befehle schicken.
 */
public class DatabaseController {

    private String connectionUrl,user,port,pass,database, message;
    protected Connection connection;
    private QueryResult currentQueryResult;

    /**
     * Erzeugt ein Database-Controller-Objekt und richtet es für die Verbindung mit einer beliebigen SQL-Datenbank ein.
     * @param serverIP die IP des SQL-Servers
     * @param port der gewünschte Port (Standard für SQL ist 1433
     * @param database der Name der Datenbank
     * @param user der Nutzername
     * @param password das Passwort für den genannten Nutzer
     */
    public DatabaseController(String serverIP, String port, String database, String user, String password) {
        connection = null;
        this.connectionUrl = serverIP;
        this.port = port;
        this.user = user;
        this.pass = password;
        this.database = database;
    }

    /**
     * Erzeugt ein Database-Controller-Objekt und richtet es für die Verbindung zur Standard-Datenbank für den
     * Informatik-Unterricht ein.
     *
     * Ein manueller Zugriff auf die Datenbank, zum Beispiel für Wartungsarbeiten ist möglich via:
     * https://webhosting24.1blu.de/1blu-dbManager/ und Nutzung von unten angegebenem Nutzer / Passwort
     */
    public DatabaseController(){
        connection = null;
        this.connectionUrl = "mysql.webhosting24.1blu.de";
        this.port = "3306";
        this.user = "s85565_2810214";
        this.pass = "aD9%informatik";
        this.database = "db85565x2810214";
    }

    /**
     * Verbindet den Server zu der bei der Initialisierung angegebenen Datenbank.
     * Baut eine bestehende Verbindung NICHT neu auf solange sie gültig ist.
     * @return gibt true zurück, falls die Verbindung erfolgreich war. Andernfalls false.
     */
    public boolean connect() {
        if(!isConnected()) {
            try {
                this.connection = DriverManager.getConnection("jdbc:mysql://"+connectionUrl+":"+port+"/"+database, user, pass);
                System.out.println("Verbindung zur Datenbank "+database+" auf Server "+connectionUrl+" erfolgreich hergestellt.");
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Prüft, ob eine valide Verbindung zur Datenbank besteht
     * @return true, falls die Verbindung besteht, andernfalls false.
     */
    public boolean isConnected(){
        if(connection != null){
            try{
                return connection.isValid(2);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Trennt die aktuelle Datenbankverbindung ohne Rücksicht auf eventuell
     * laufende Kommunikationsprozesse.
     */
    public void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //***************** Die folgenden Methoden sind identisch zur Abiturklasse "DatabaseConnector" ******************

    /**
     * Der Auftrag schickt den im Parameter pSQLStatement enthaltenen SQL-Befehl an die
     * Datenbank ab.
     * Handelt es sich bei pSQLStatement um einen SQL-Befehl, der eine Ergebnismenge
     * liefert, so kann dieses Ergebnis anschließend mit der Methode getCurrentQueryResult
     * abgerufen werden.
     */
    public void executeStatement(String pSQLStatement){
        //Altes Ergebnis loeschen
        currentQueryResult = null;
        message = null;

        try {
            //Neues Statement erstellen
            Statement statement = connection.createStatement();

            //SQL Anweisung an die DB schicken.
            if (statement.execute(pSQLStatement)) { //Fall 1: Es gibt ein Ergebnis

                //Resultset auslesen
                ResultSet resultset = statement.getResultSet();

                //Spaltenanzahl ermitteln
                int columnCount = resultset.getMetaData().getColumnCount();

                //Spaltennamen und Spaltentypen in Felder uebertragen
                String[] resultColumnNames = new String[columnCount];
                String[] resultColumnTypes = new String[columnCount];
                for (int i = 0; i < columnCount; i++){
                    resultColumnNames[i] = resultset.getMetaData().getColumnLabel(i+1);
                    resultColumnTypes[i] = resultset.getMetaData().getColumnTypeName(i+1);
                }

                //Queue fuer die Zeilen der Ergebnistabelle erstellen
                Queue<String[]> rows = new Queue<>();

                //Daten in Queue uebertragen und Zeilen zaehlen
                int rowCount = 0;
                while (resultset.next()){
                    String[] resultrow =  new String[columnCount];
                    for (int s = 0; s < columnCount; s++){
                        resultrow[s] = resultset.getString(s+1);
                    }
                    rows.enqueue(resultrow);
                    rowCount = rowCount + 1;
                }

                //Ergebnisfeld erstellen und Zeilen aus Queue uebertragen
                String[][] resultData = new String[rowCount][columnCount];
                int j = 0;
                while (!rows.isEmpty()){
                    resultData[j] = rows.front();
                    rows.dequeue();
                    j = j + 1;
                }

                //Statement schließen und Ergebnisobjekt erstellen
                statement.close();
                currentQueryResult =  new QueryResult(resultData, resultColumnNames, resultColumnTypes);

            } else { //Fall 2: Es gibt kein Ergebnis.
                //Statement ohne Ergebnisobjekt schliessen
                statement.close();
            }

        } catch (Exception e) {
            //Fehlermeldung speichern
            message = e.getMessage();
        }
    }

    /**
     * Die Anfrage liefert das Ergebnis des letzten mit der Methode executeStatement an
     * die Datenbank geschickten SQL-Befehls als Ob-jekt vom Typ QueryResult zurueck.
     * Wurde bisher kein SQL-Befehl abgeschickt oder ergab der letzte Aufruf von
     * executeStatement keine Ergebnismenge (z.B. bei einem INSERT-Befehl oder einem
     * Syntaxfehler), so wird null geliefert.
     */
    public QueryResult getCurrentQueryResult(){
        return currentQueryResult;
    }

    /**
     * Die Anfrage liefert null oder eine Fehlermeldung, die sich jeweils auf die letzte zuvor ausgefuehrte
     * Datenbankoperation bezieht.
     */
    public String getErrorMessage(){
        return message;
    }

    //***************** Geräteverleih *****************

    // Name der ID-Spalte in 26_arn_mitarbeiter. Wegen des Bindestrichs mit Backticks (`) geschrieben.
    private static final String EMPLOYEE_ID = "`Mitarbeiter-ID`";

    /**
     * @return Zeilen mit den Spalten: ID, Typ, Bezeichnung, Verfügbar
     */
    public String[][] loadDevices() {
        return queryRows(
                "SELECT g.ID, g.Typ, g.Bezeichnung, g.Gesamtanzahl - COUNT(v.ID) AS Verfuegbar " +
                        "FROM 26_nai_geraete g " +
                        "LEFT JOIN 26_nai_verliehene_geraete v ON v.Geraet_ID = g.ID " +
                        "GROUP BY g.ID, g.Typ, g.Bezeichnung, g.Gesamtanzahl " +
                        "ORDER BY g.Typ, g.Bezeichnung;");
    }

    /**
     * @return Zeilen mit den Spalten: Verleih-ID, Bezeichnung, Typ, Vorname, Datum
     */
    public String[][] loadLoans() {
        return queryRows(
                "SELECT v.ID, g.Bezeichnung, g.Typ, m.Vorname, DATE_FORMAT(v.Ausgeliehen_am, '%d.%m.%Y') " +
                        "FROM 26_nai_verliehene_geraete v " +
                        "JOIN 26_nai_geraete g ON v.Geraet_ID = g.ID " +
                        "JOIN 26_arn_mitarbeiter m ON v.Mitarbeiter_ID = m." + EMPLOYEE_ID + " " +
                        "ORDER BY v.Ausgeliehen_am DESC;");
    }

    /**
     * @return Zeilen mit den Spalten: ID, Vorname
     */
    public String[][] loadEmployees() {
        return queryRows("SELECT " + EMPLOYEE_ID + ", Vorname FROM 26_arn_mitarbeiter ORDER BY Vorname;");
    }

    /**
     * Verleiht ein Stück des Geräts an einen Mitarbeiter.
     *
     * @return null, wenn alles geklappt hat, sonst eine Fehlermeldung zum Anzeigen
     */
    public String lendDevice(int deviceId, int employeeId) {
        // Zuerst prüfen, ob noch ein Stück verfügbar ist
        String[][] check = queryRows(
                "SELECT g.Gesamtanzahl - COUNT(v.ID) " +
                        "FROM 26_nai_geraete g " +
                        "LEFT JOIN 26_nai_verliehene_geraete v ON v.Geraet_ID = g.ID " +
                        "WHERE g.ID = " + deviceId + " " +
                        "GROUP BY g.ID, g.Gesamtanzahl;");
        if (check.length == 0) {
            return "Gerät nicht gefunden.";
        }
        if (Integer.parseInt(check[0][0]) <= 0) {
            return "Dieses Gerät ist aktuell nicht verfügbar.";
        }

        executeStatement(
                "INSERT INTO 26_nai_verliehene_geraete (Geraet_ID, Mitarbeiter_ID) " +
                        "VALUES (" + deviceId + ", " + employeeId + ");");
        if (getErrorMessage() != null) {
            return "Datenbankfehler: " + getErrorMessage();
        }
        return null;
    }

    /**
     * Nimmt ein verliehenes Gerät zurück (löscht den Verleih-Eintrag).
     *
     * @return null, wenn alles geklappt hat, sonst eine Fehlermeldung zum Anzeigen
     */
    public String returnDevice(int loanId) {
        executeStatement("DELETE FROM 26_nai_verliehene_geraete WHERE ID = " + loanId + ";");
        if (getErrorMessage() != null) {
            return "Datenbankfehler: " + getErrorMessage();
        }
        return null;
    }

    /**
     * Schickt eine SELECT-Abfrage an die Datenbank und liefert die Ergebniszeilen.
     * Bei einem Fehler wird die Meldung in der Konsole ausgegeben und ein leeres Feld geliefert.
     */
    private String[][] queryRows(String sql) {
        executeStatement(sql);
        QueryResult result = getCurrentQueryResult();
        if (result == null) {
            System.out.println("Datenbankfehler: " + getErrorMessage() + "  (Abfrage: " + sql + ")");
            return new String[0][0];
        }
        return result.getData();
    }

}