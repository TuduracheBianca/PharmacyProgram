package domain;

import eu.hansolo.toolbox.time.DateTimes;

import java.util.Locale;
import java.util.UUID;

public class Medication {
    //    private UUID id;
    private String name;
    private String description;
    private String unitOfMeasure;
    private int availability;
    private String Manufacturer;
    private String Category;


    public Medication(String name, String description, String unitOfMeasure, int availability, String Manufacturer, String Category) {
//        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
        this.availability = availability;
        this.Manufacturer = Manufacturer;
        this.Category = Category;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

//    public UUID getId() {
//        return id;
//    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public String getName() {
        return name;
    }

//    public String setName(String name) {
//        this.name = name;
//    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return Category;
    }
//    public String setDescription(String description) {
//        this.description = description;
//    }
    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
    public int getAvailability() {
        return availability;
    }

    public void setQuantity(int quantity) {
        this.availability = quantity;
    }
}
