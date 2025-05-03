package domain;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OrderItem {
    private int id;
    private final IntegerProperty medicationId = new SimpleIntegerProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    // Added properties for display in table
    private final StringProperty medicationName = new SimpleStringProperty();
    private final StringProperty unitOfMeasure = new SimpleStringProperty();

    public OrderItem(int medicationId, int quantity) {
        this.medicationId.set(medicationId);
        this.quantity.set(quantity);
    }

    public OrderItem(int id, int medicationId, int quantity) {
        this.id = id;
        this.medicationId.set(medicationId);
        this.quantity.set(quantity);
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMedicationId() {
        return medicationId.get();
    }

    public IntegerProperty medicationIdProperty() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId.set(medicationId);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    // Added getters and setters for display properties
    public String getMedicationName() {
        return medicationName.get();
    }

    public StringProperty medicationNameProperty() {
        return medicationName;
    }

    public void setMedicationName(String name) {
        this.medicationName.set(name);
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure.get();
    }

    public StringProperty unitOfMeasureProperty() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unit) {
        this.unitOfMeasure.set(unit);
    }
}