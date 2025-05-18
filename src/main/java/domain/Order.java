package domain;

import javafx.beans.property.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private final IntegerProperty id;
    private final Date orderDate;
    private final StringProperty orderDateString;
    private final StringProperty status;
    private final BooleanProperty urgent;
    private List<OrderItem> orderItems;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Order(int id, Date orderDate, String status, boolean urgent) {
        this.id = new SimpleIntegerProperty(id);
        this.orderDate = orderDate;
        this.orderDateString = new SimpleStringProperty(formatDate(orderDate));
        this.status = new SimpleStringProperty(status);
        this.urgent = new SimpleBooleanProperty(urgent);
        this.orderItems = new ArrayList<>();
    }

    private String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }

    // Regular getters for non-property access
    public int getId() {
        return id.get();
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status.get();
    }

    public boolean isUrgent() {
        return urgent.get();
    }

    // Property getters for JavaFX bindings
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty orderDateProperty() {
        return orderDateString;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public BooleanProperty urgentProperty() {
        return urgent;
    }

    // Setters
    public void setStatus(String status) {
        this.status.set(status);
    }

    public void setUrgent(boolean urgent) {
        this.urgent.set(urgent);
    }

    // Order items management
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
    }

    public void setItems(List<OrderItem> items) {
        this.orderItems = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + getId() +
                ", orderDate=" + orderDate +
                ", status='" + getStatus() + '\'' +
                ", urgent=" + isUrgent() +
                ", items=" + orderItems.size() +
                '}';
    }
}