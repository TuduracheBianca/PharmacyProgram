package service;

import domain.Medication;
import domain.Order;
import domain.OrderItem;
import repository.SQLMedicationRepository;
import repository.SQLOrderRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {
    private final MedicationService medicationService;
    private final SQLOrderRepository orderRepository;
    private final SQLMedicationRepository medicationRepository;
    private List<OrderItem> currentOrderItems;
    private boolean isUrgent;

    /**
     * Constructor that accepts only MedicationService
     * Creates its own instances of repositories
     */
    public OrderService(MedicationService medicationService) {
        this.medicationService = medicationService;
        this.orderRepository = new SQLOrderRepository();
        this.medicationRepository = (SQLMedicationRepository) medicationService.getRepository();
        this.currentOrderItems = new ArrayList<>();
        this.isUrgent = false;
    }

    /**
     * Full constructor with all dependencies
     */
    public OrderService(MedicationService medicationService,
                        SQLOrderRepository orderRepository,
                        SQLMedicationRepository medicationRepository) {
        this.medicationService = medicationService;
        this.orderRepository = orderRepository;
        this.medicationRepository = medicationRepository;
        this.currentOrderItems = new ArrayList<>();
        this.isUrgent = false;
    }

    /**
     * Sets if the current order is urgent
     *
     * @param urgent True if the order is urgent
     */
    public void setUrgent(boolean urgent) {
        this.isUrgent = urgent;
    }

    /**
     * Checks if the current order is marked as urgent
     *
     * @return True if the order is urgent
     */
    public boolean isUrgent() {
        return isUrgent;
    }

    /**
     * Adds a medication to the current order
     *
     * @param medicationName The name of the medication
     * @param quantity The quantity to add
     * @throws IllegalArgumentException If the medication is not found or not enough stock
     */
    public void addMedicationToOrder(String medicationName, int quantity) {
        Medication medication = medicationService.findMedicationByName(medicationName);

        if (medication == null) {
            throw new IllegalArgumentException("Medicamentul " + medicationName + " nu a fost găsit");
        }

        if (medication.getAvailability() < quantity) {
            throw new IllegalArgumentException("Nu există stoc suficient pentru " + medicationName);
        }

        // Get medication ID
        int medicationId = getMedicationId(medication);

        // Check if medication is already in order
        for (OrderItem item : currentOrderItems) {
            if (item.getMedicationName().equals(medicationName)) {
                int newQuantity = item.getQuantity() + quantity;

                if (medication.getAvailability() < newQuantity) {
                    throw new IllegalArgumentException("Nu există stoc suficient pentru " + medicationName);
                }

                item.setQuantity(newQuantity);
                return;
            }
        }

        // If not found, add as new item
        OrderItem newItem = new OrderItem(medicationId, quantity);
        // Set the medication name and unit for display purposes
        newItem.setMedicationName(medicationName);
        newItem.setUnitOfMeasure(medication.getUnitOfMeasure());
        currentOrderItems.add(newItem);
    }

    public Order getOrderDetailsForItem(OrderItem item) throws SQLException {
        // Get all orders from the repository
        List<Order> allOrders = orderRepository.getOrders();

        // Search through all orders to find the one containing our item
        for (Order order : allOrders) {
            // Get all items for this order
            List<OrderItem> orderItems = orderRepository.getOrderItems(order.getId());

            // Check if our item is in this order
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getMedicationId() == item.getMedicationId() &&
                        orderItem.getQuantity() == item.getQuantity()) {
                    // Found our order - set the items and return
                    order.setItems(orderItems);
                    return order;
                }
            }
        }

        throw new IllegalArgumentException("Item not found in any order");
    }

    public List<Order> getAllOrdersSortedByUrgencyAndDate() {
        List<Order> orders = orderRepository.getOrders();

        // Sort the orders: first by urgency (urgent first) then by date (newer first)
        return orders.stream()
                .sorted(
                        // First sort by urgency (urgent first)
                        Comparator.comparing(Order::isUrgent).reversed()
                                // Then sort by date (newer first)
                                .thenComparing(Comparator.comparing(Order::getOrderDate).reversed())
                )
                .collect(Collectors.toList());
    }

    /**
     * Gets all items in the current order
     *
     * @return List of order items
     */
    public List<OrderItem> getCurrentOrderItems() {
        return currentOrderItems;
    }

    /**
     * Places the current order, updating medication stocks and creating database records
     *
     * @return The ID of the placed order
     * @throws IllegalArgumentException If the order is empty
     */
    public int placeOrder() {
        if (currentOrderItems.isEmpty()) {
            throw new IllegalArgumentException("Comanda este goală");
        }

        try {
            // Create new order in database
            int orderId = orderRepository.createOrder(isUrgent);

            // Add items to order
            for (OrderItem item : currentOrderItems) {
                // Add to database
                orderRepository.addOrderItem(orderId, item.getMedicationId(), item.getQuantity());

                // Update stock for each medication
                Medication medication = medicationService.findMedicationById(item.getMedicationId());
                if (medication != null) {
                    medicationService.updateMedicationStock(medication.getName(), item.getQuantity());
                }
            }

            // Clear the current order after successful placement
            clearOrder();

            return orderId;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Eroare la plasarea comenzii: " + e.getMessage());
        }
    }

    /**
     * Clears the current order without placing it
     */
    public void clearOrder() {
        currentOrderItems.clear();
        isUrgent = false;
    }

    /**
     * Retrieves all orders from the repository
     *
     * @return List of all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.getOrders();
    }

    /**
     * Gets an order by its ID
     *
     * @param orderId The order ID
     * @return The order, or null if not found
     */
    public Order getOrderById(int orderId) {
        return orderRepository.getOrderById(orderId);
    }

    /**
     * Updates the status of an order
     *
     * @param orderId The order ID
     * @param status The new status
     */
    public void updateOrderStatus(int orderId, String status) {
        try {
            orderRepository.updateOrderStatus(orderId, status);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Eroare la actualizarea statusului comenzii: " + e.getMessage());
        }
    }

    /**
     * Updates the urgency of an order
     *
     * @param orderId The order ID
     * @param urgent Whether the order is urgent
     */
    public void updateOrderUrgency(int orderId, boolean urgent) {
        try {
            orderRepository.updateOrderUrgency(orderId, urgent);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Eroare la actualizarea urgenței comenzii: " + e.getMessage());
        }
    }

    /**
     * Helper method to get medication ID from a Medication object
     */
    private int getMedicationId(Medication medication) {
        List<Medication> allMedications = medicationRepository.getMedications();
        for (int i = 0; i < allMedications.size(); i++) {
            if (allMedications.get(i).getName().equals(medication.getName())) {
                return i + 1; // Assuming IDs start from 1
            }
        }
        return -1; // Not found
    }

    public void removeMedicationFromOrder(String medicationName) {
        boolean removed = currentOrderItems.removeIf(item -> item.getMedicationName().equalsIgnoreCase(medicationName));
        if (!removed) {
            throw new IllegalArgumentException("Medicamentul " + medicationName + " nu există în comandă.");
        }
    }

    public boolean deleteOrder(int orderId) {
        Order orderToDelete = getOrderById(orderId);
        if (orderToDelete == null) {
            return false;
        }

        try {
            return orderRepository.deleteOrder(orderId);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Eroare la ștergerea comenzii: " + e.getMessage());
        }
    }

}