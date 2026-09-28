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


/**
 * Ein Objekt der Klasse ProgramController dient dazu das Programm zu steuern.
 * Hinweise:
 * - Der Konstruktor sollte nicht geändert werden.
 * - Sowohl die startProgram()- als auch die updateProgram(...)-Methoden müssen vorhanden sein und ihre Signatur sollte
 *   nicht geändert werden
 * - Zusätzliche Methoden sind natürlich gar kein Problem
 */
public class ProgramController {

    //Attribute


    // Referenzen
    private final ViewController viewController;  // diese Referenz soll auf ein Objekt der Klasse viewController zeigen. Über dieses Objekt wird das Fenster gesteuert.
    private final BueroMap plan = new BueroMap(0, 0);
    private List<Desk> deskList;
    public String currentEmployeeName;

    /**
     * Konstruktor
     * Dieser legt das Objekt der Klasse ProgramController an, das den Programmfluss steuert.
     * Damit der ProgramController auf das Fenster zugreifen kann, benötigt er eine Referenz auf das Objekt
     * der Klasse viewController. Diese wird als Parameter übergeben.
     *
     * @param viewController das viewController-Objekt des Programms
     */
    public ProgramController(ViewController viewController) {
        this.deskList = new List<Desk>();
        this.viewController = viewController;
    }

    public void startProgram() {
        showLoginPanel();
        viewController.draw(plan.drawMap());
        addDeskToBuero("1", 5, 0, 92, 40);
        addDeskToBuero("1", 115, 0, 92, 40);
        addDeskToBuero("1", 315, 0, 92, 40);
        addDeskToBuero("1", 418, 15, 46, 40);
        addDeskToBuero("1", 530, 15, 46, 40);
        addDeskToBuero("1", 640, 15, 46, 40);
        addDeskToBuero("1", 750, 15, 46, 40);
        addDeskToBuero("1", 860, 15, 46, 40);
        addDeskToBuero("1", 3, 350, 46, 40);
        addDeskToBuero("1", 3, 400, 46, 40);
        addDeskToBuero("1", 5, 565, 92, 40);
        addDeskToBuero("1", 260, 565, 46, 40);
        addDeskToBuero("1", 375, 565, 46, 40);
        addDeskToBuero("1", 486, 565, 46, 40);
        addDeskToBuero("1", 935, 512, 40, 60);
        addDeskToBuero("1", 170, 200, 55, 35);
        addDeskToBuero("1", 260, 200, 55, 35);
        addDeskToBuero("1", 172, 387, 55, 35);
        addDeskToBuero("1", 259, 387, 55, 35);


    }

    private void showLoginPanel() {
        String input = JOptionPane.showInputDialog(
                null, "Bitte gibt deinen Namen ein"
        );

        currentEmployeeName = input;
    }

    private void addDeskToBuero(String id, double x, double y, double width, double height) {
        Desk newDeskModel = new Desk(id, x, y, width, height);
        deskList.append(newDeskModel);
        viewController.draw(new DeskView(newDeskModel));
    }


    /**
     * Diese Methode wird genau ein mal nach Programmstart aufgerufen. Hier sollte also alles geregelt werden,
     * was zu diesem Zeipunkt passieren muss.
     */


    /**
     * Diese Methode wird vom ViewController-Objekt automatisch mit jedem Frame aufgerufen (ca. 60mal pro Sekunde)
     *
     * @param dt Zeit seit letztem Frame in Sekunden
     */
    public void updateProgram(double dt) {

    }


    public void mouseClicked(MouseEvent e) {
        System.out.println("yeas");
        double mouseX = e.getX();
        double mouseY = e.getY();


        deskList.toFirst();
        while (deskList.hasAccess()) {
            Desk currentDesk = deskList.getContent();

            if (mouseX >= currentDesk.getX() && mouseX <= currentDesk.getX() + currentDesk.getWidth() &&
                    mouseY >= currentDesk.getY() && mouseY <= currentDesk.getY() + currentDesk.getHeight()) {

                currentDesk.toggleReservation(currentEmployeeName);


                // 2. Später für SQL:
                // updateSQL(currentDesk);

                break;
            }

            deskList.next();
        }
    }

}



