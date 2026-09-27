package my_project.view;

import KAGO_framework.model.GraphicalObject;

public class BueroMap{
    private double x,y;

    public BueroMap(double px, double py){
        this.x = px;
        this.y = py;
    }

    public GraphicalObject drawMap() {
        GraphicalObject map = new GraphicalObject("src/main/resources/graphic/floorplan.png");
        map.setX(x);
        map.setY(y);

        return map;
    }

}
