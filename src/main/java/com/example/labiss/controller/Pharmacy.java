package com.example.labiss.controller;

import domain.Medication;
import domain.Order;
import domain.OrderItem;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import repository.SQLMedicationRepository;
import service.MedicationService;
import service.OrderService;

public class Pharmacy {
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> colOrderId;
    @FXML
    private TableColumn<Order, String> colOrderDate;
    @FXML
    private TableColumn<Order, String> colOrderStatus;
    @FXML
    private TableColumn<Order, String> colOrderUrgent;

    @FXML
    private TableView<OrderItemWithMedication> itemsTable;
    @FXML
    private TableColumn<OrderItemWithMedication, String> colItemName;
    @FXML
    private TableColumn<OrderItemWithMedication, String> colItemUnit;
    @FXML
    private TableColumn<OrderItemWithMedication, Integer> colItemQuantity;

    // Helper class to display medication info with quantity
    public static class OrderItemWithMedication {
        private final SimpleStringProperty medicationName;
        private final SimpleStringProperty unitOfMeasure;
        private final SimpleIntegerProperty quantity;

        public OrderItemWithMedication(String medicationName, String unitOfMeasure, int quantity) {
            this.medicationName = new SimpleStringProperty(medicationName);
            this.unitOfMeasure = new SimpleStringProperty(unitOfMeasure);
            this.quantity = new SimpleIntegerProperty(quantity);
        }

        public String getMedicationName() { return medicationName.get(); }
        public String getUnitOfMeasure() { return unitOfMeasure.get(); }
        public int getQuantity() { return quantity.get(); }

        // Add these property accessor methods for JavaFX
        public SimpleStringProperty medicationNameProperty() { return medicationName; }
        public SimpleStringProperty unitOfMeasureProperty() { return unitOfMeasure; }
        public SimpleIntegerProperty quantityProperty() { return quantity; }
    }

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button Orders;

    private OrderService orderService;
    private MedicationService medicationService;
    private ObservableList<Order> orders;

    @FXML
    public void initialize() {
        // Initialize services
        medicationService = new MedicationService(new SQLMedicationRepository());
        this.orderService = new OrderService(medicationService);

        // Configure orders table
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colOrderUrgent.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isUrgent() ? "URGENT" : "Normal"));

        // Configure items table - use property accessor methods instead
        colItemName.setCellValueFactory(cellData -> cellData.getValue().medicationNameProperty());
        colItemUnit.setCellValueFactory(cellData -> cellData.getValue().unitOfMeasureProperty());
        colItemQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        // Setup double-click handler
        setupDoubleClickHandler();

        // Configure status combo box
        statusComboBox.getItems().addAll("PENDING", "PROCESSING", "COMPLETED", "CANCELLED");
        statusComboBox.setVisible(false);
        statusComboBox.setOnAction(event -> updateOrderStatus());

        // Initially hide tables
        ordersTable.setVisible(false);
        itemsTable.setVisible(false);
    }

    private void loadOrders() {
        try {
            orders = FXCollections.observableArrayList(orderService.getAllOrdersSortedByUrgencyAndDate());
            ordersTable.setItems(orders);
            ordersTable.setVisible(true);
            itemsTable.setVisible(false); // Hide medications table when loading orders
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error loading orders: " + e.getMessage());
        }
    }

    private void setupDoubleClickHandler() {
        ordersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Order selectedOrder = row.getItem();
                    showOrderMedications(selectedOrder);

                    // Show status combo box for pharmacy
                    statusComboBox.setVisible(true);
                    statusComboBox.setValue(selectedOrder.getStatus());

                    // Show medications table
                    itemsTable.setVisible(true);
                }
            });
            return row;
        });
    }

    private void showOrderMedications(Order order) {
        try {
            // Get the medications from order items
            ObservableList<OrderItemWithMedication> medicationItems = FXCollections.observableArrayList();

            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                System.out.println("No items found in order #" + order.getId());
                return;
            }

            System.out.println("Processing " + order.getOrderItems().size() + " items for order #" + order.getId());

            for (OrderItem item : order.getOrderItems()) {
                try {
                    // Fetch the medication using the medicationId from the OrderItem
                    int medicationId = item.getMedicationId();
                    System.out.println("Looking up medication ID: " + medicationId);

                    Medication medication = medicationService.findMedicationById(medicationId);

                    if (medication != null) {
                        // Create an OrderItemWithMedication to display both medication info and quantity
                        OrderItemWithMedication displayItem = new OrderItemWithMedication(
                                medication.getName(),
                                medication.getUnitOfMeasure(),
                                item.getQuantity()
                        );
                        medicationItems.add(displayItem);
                        System.out.println("Added medication: " + medication.getName());
                    } else {
                        // If medication not found, show with placeholder info
                        OrderItemWithMedication displayItem = new OrderItemWithMedication(
                                "ID: " + item.getMedicationId() + " (Not found)",
                                "N/A",
                                item.getQuantity()
                        );
                        medicationItems.add(displayItem);
                        System.out.println("Medication not found for ID: " + medicationId);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading medication ID " + item.getMedicationId() + ": " + e.getMessage());
                    e.printStackTrace();
                    // Add a placeholder entry to show there was an error
                    OrderItemWithMedication displayItem = new OrderItemWithMedication(
                            "Error loading ID: " + item.getMedicationId(),
                            "Error",
                            item.getQuantity()
                    );
                    medicationItems.add(displayItem);
                }
            }

            itemsTable.setItems(medicationItems);
            System.out.println("Set " + medicationItems.size() + " items in the table");

            // Make sure the items table is visible
            itemsTable.setVisible(true);

            // Refresh the table to ensure it displays the data
            itemsTable.refresh();

        } catch (Exception e) {
            System.err.println("Error in showOrderMedications: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error showing medications: " + e.getMessage());
        }
    }

    private void updateOrderStatus() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            String newStatus = statusComboBox.getValue();
            try {
                orderService.updateOrderStatus(selectedOrder.getId(), newStatus);
                selectedOrder.setStatus(newStatus);
                ordersTable.refresh();
            } catch (Exception e) {
                System.err.println("Error updating order status: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error updating order status: " + e.getMessage());
            }
        }
    }

    @FXML
    public void ShowOrders(ActionEvent event) {
        loadOrders();
        statusComboBox.setVisible(false);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}