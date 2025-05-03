package domain;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Medication {
    private final StringProperty name;
    private final StringProperty description;
    private final StringProperty unitOfMeasure;
    private final IntegerProperty availability;
    private final StringProperty manufacturer;
    private final StringProperty category;

    public Medication(String name, String description, String unitOfMeasure, int availability,
                      String manufacturer, String category) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.unitOfMeasure = new SimpleStringProperty(unitOfMeasure);
        this.availability = new SimpleIntegerProperty(availability);
        this.manufacturer = new SimpleStringProperty(manufacturer);
        this.category = new SimpleStringProperty(category);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure.get();
    }

    public StringProperty unitOfMeasureProperty() {
        return unitOfMeasure;
    }

    public int getAvailability() {
        return availability.get();
    }

    public void setAvailability(int availability) {
        this.availability.set(availability);
    }

    public IntegerProperty availabilityProperty() {
        return availability;
    }

    public String getManufacturer() {
        return manufacturer.get();
    }

    public StringProperty manufacturerProperty() {
        return manufacturer;
    }

    public String getCategory() {
        return category.get();
    }

    public StringProperty categoryProperty() {
        return category;
    }
}