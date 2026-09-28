package my_project.model;

public class Device {
    private String id;
    private boolean isReserved;
    private String reservedBy;
    private double x, y, width, height; // Die reinen Positionsdaten

    public Device(String id, double x, double y, double width, double height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isReserved = false;
    }

    public void toggleReservation(String employeeName) {
        this.isReserved = !this.isReserved;
        if (this.isReserved) {
            this.reservedBy = employeeName; // Set name on reservation
        } else {
            this.reservedBy = ""; // Clear name on cancel
        }
    }

    // Getter und Setter
    public String getId() { return id; }
    public boolean isReserved() { return isReserved; }
    public void setReserved(boolean reserved) { this.isReserved = reserved; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
