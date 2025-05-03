package domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private Date orderDate;
    private String status;
    private boolean urgent;
    private List<OrderItem> orderItems;

    public Order(int id, Date orderDate, String status, boolean urgent) {
        this.id = id;
        this.orderDate = orderDate;
        this.status = status;
        this.urgent = urgent;
        this.orderItems = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderDate=" + orderDate +
                ", status='" + status + '\'' +
                ", urgent=" + urgent +
                ", items=" + orderItems.size() +
                '}';
    }
}
