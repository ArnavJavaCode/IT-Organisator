package my_project.view;

import KAGO_framework.model.InteractiveGraphicalObject;
import KAGO_framework.view.DrawTool;
import my_project.model.Desk;
import my_project.control.ProgramController;
import java.awt.Color;
import java.awt.event.MouseEvent;

public class DeskView extends InteractiveGraphicalObject {

    private Desk myDesk;

    public DeskView(Desk model) {
        this.myDesk = model;
    }

    @Override
    public void draw(DrawTool drawTool) {
        if (myDesk.isReserved()) {
            drawTool.setCurrentColor(new Color(255, 0, 0, 100));
        } else {
            drawTool.setCurrentColor(new Color(0, 255, 0, 60));
        }

        drawTool.drawFilledRectangle(myDesk.getX(), myDesk.getY(), myDesk.getWidth(), myDesk.getHeight());
        drawTool.setCurrentColor(myDesk.isReserved() ? Color.RED : Color.GREEN);
        drawTool.drawRectangle(myDesk.getX(), myDesk.getY(), myDesk.getWidth(), myDesk.getHeight());
        drawTool.setCurrentColor(Color.YELLOW);
        drawTool.drawText(myDesk.getX() + 25, myDesk.getY() + 25, myDesk.getId());
    }
}
