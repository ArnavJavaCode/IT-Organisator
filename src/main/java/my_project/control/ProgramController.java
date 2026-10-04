package my_project.control;

import KAGO_framework.control.DatabaseController;
import KAGO_framework.control.ViewController;
import KAGO_framework.model.abitur.datenbanken.mysql.DatabaseConnector;
import KAGO_framework.model.abitur.datenstrukturen.List;
import my_project.model.Desk;
import my_project.view.BueroMap;
import my_project.view.DeskView;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Random;


/**
 * Ein Objekt der Klasse ProgramController dient dazu das Programm zu steuern.
 * Hinweise:
 * - Der Konstruktor sollte nicht geändert werden.
 * - Sowohl die startProgram()- als auch die updateProgram(...)-Methoden müssen vorhanden sein und ihre Signatur sollte
 *   nicht geändert werden
 * - Zusätzliche Methoden sind natürlich gar kein Problem
 */
public class ProgramController {



        // Attribute
        private final ViewController viewController;
        private final BueroMap plan = new BueroMap(0, 0);
        private List<Desk> deskList;

        public String currentEmployeeName;
        private int currentMitarbeiterId;
        private DatabaseController db;

        private int nextDeskId = 1;

        /**
         * Konstruktor
         */
        public ProgramController(ViewController viewController) {
            this.deskList = new List<Desk>();
            this.viewController = viewController;


            db = new DatabaseController("mysql.webhosting24.1blu.de", "3306", "db85565x2810214", "s85565_2810214", "locker1337SQLproggen!");
            db.connect();
        }

        public void startProgram() {
            showLoginPanel();
            viewController.draw(plan.drawMap());


            addDeskToBuero(5, 0, 92, 40);
            addDeskToBuero(115, 0, 92, 40);
            addDeskToBuero(315, 0, 92, 40);
            addDeskToBuero(418, 15, 46, 40);
            addDeskToBuero(530, 15, 46, 40);
            addDeskToBuero(640, 15, 46, 40);
            addDeskToBuero(750, 15, 46, 40);
            addDeskToBuero(860, 15, 46, 40);
            addDeskToBuero(3, 350, 46, 40);
            addDeskToBuero(3, 400, 46, 40);
            addDeskToBuero(5, 565, 92, 40);
            addDeskToBuero(260, 565, 46, 40);
            addDeskToBuero(375, 565, 46, 40);
            addDeskToBuero(486, 565, 46, 40);
            addDeskToBuero(935, 512, 40, 60);
            addDeskToBuero(170, 200, 55, 35);
            addDeskToBuero(260, 200, 55, 35);
            addDeskToBuero(172, 387, 55, 35);
            addDeskToBuero(259, 387, 55, 35);
        }

        private void showLoginPanel() {
            String input = JOptionPane.showInputDialog(
                    null, "Bitte gib deinen Namen ein"
            );

            if (input == null || input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Name erforderlich! Programm beendet.", "Fehler", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            currentEmployeeName = input.trim();


            String login = "INSERT INTO 26_arn_mitarbeiter (Vorname, Abteilung) VALUES ('" + currentEmployeeName + "', 'Büro');";
            db.executeStatement(login);

            db.executeStatement("SELECT LAST_INSERT_ID();");
            if (db.getCurrentQueryResult() != null && db.getCurrentQueryResult().getData().length > 0) {
                this.currentMitarbeiterId = Integer.parseInt(db.getCurrentQueryResult().getData()[0][0]);
            } else {
                this.currentMitarbeiterId = 1;
            }

        }

        private void addDeskToBuero(double x, double y, double width, double height) {
            String generatedId = String.valueOf(nextDeskId);

            Desk newDeskModel = new Desk(generatedId, x, y, width, height);
            deskList.append(newDeskModel);

            viewController.draw(new DeskView(newDeskModel));

            nextDeskId++;
        }

        public void updateProgram(double dt) {

        }


        public void mouseClicked(MouseEvent e) {
            double mouseX = e.getX();
            double mouseY = e.getY();

            deskList.toFirst();
            while (deskList.hasAccess()) {
                Desk currentDesk = deskList.getContent();

                if (mouseX >= currentDesk.getX() && mouseX <= currentDesk.getX() + currentDesk.getWidth() &&
                        mouseY >= currentDesk.getY() && mouseY <= currentDesk.getY() + currentDesk.getHeight()) {

                    currentDesk.toggleReservation();
                    String fremdschluesselId = currentDesk.isReserved() ? String.valueOf(this.currentMitarbeiterId) : "NULL";

                    String sqlBefehl = "INSERT INTO 26_arn_reserv_sitzplaetze (`Sitz-ID`, `Mitarbeiter_ID`) " +
                            "VALUES (" + currentDesk.getId() + ", " + fremdschluesselId + ");";

                    db.executeStatement(sqlBefehl);

                    if (db.getErrorMessage() != null) {
                        System.err.println("Fehler: " + db.getErrorMessage());
                    } else {
                        System.out.println("Kein Fehler!!!");
                    }

                    break;
                }

                deskList.next();
            }
        }
    }