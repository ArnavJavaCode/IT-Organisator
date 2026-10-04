package my_project.model;

/**
 * Ein Eintrag der Mitarbeiter-Auswahlliste: zeigt den Namen an, merkt sich aber die ID.
 */
public class EmpolyeeItem {
    private final int id;
    private final String name;

    public EmpolyeeItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Die ComboBox zeigt das Ergebnis von toString() an
    @Override
    public String toString() {
        return name;
    }
}