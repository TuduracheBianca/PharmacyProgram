package service;

import domain.Medication;
import repository.SQLMedicationRepository;

import java.sql.SQLException;
import java.util.List;

public class MedicationService {
    private final SQLMedicationRepository repository;

    public MedicationService(SQLMedicationRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the underlying repository
     */
    public SQLMedicationRepository getRepository() {
        return repository;
    }

    /**
     * Get all medications from the repository
     */
    public List<Medication> getAllMedications() {
        return repository.getMedications();
    }

    /**
     * Find a medication by its name
     */
    public Medication findMedicationByName(String name) {
        for (Medication medication : repository.getMedications()) {
            if (medication.getName().equals(name)) {
                return medication;
            }
        }
        return null;
    }

    /**
     * Find a medication by its ID
     */
    public Medication findMedicationById(int id) {
        List<Medication> medications = repository.getMedications();
        if (id > 0 && id <= medications.size()) {
            return medications.get(id - 1); // Assuming IDs start from 1
        }
        return null;
    }



    /**
     * Update medication stock after an order
     */
    public void updateMedicationStock(String medicationName, int quantity) {
        Medication medication = findMedicationByName(medicationName);
        if (medication != null) {
            try {
                int newStock = medication.getAvailability() - quantity;
                if (newStock < 0) {
                    throw new IllegalArgumentException("Nu există stoc suficient pentru " + medicationName);
                }
                repository.updateMedicationStock(repository.findMedicationIdByName(medicationName), newStock);
                medication.setAvailability(newStock);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Eroare la actualizarea stocului: " + e.getMessage());
            }
        }
    }

//    public void reserveStock(String medicationName, int quantity) {
//        try {
//            repository.reserveStock(medicationName, quantity);
//        } catch (SQLException e) {
//            throw new RuntimeException("Eroare la rezervarea stocului", e);
//        }
//    }
//
//    public void confirmStockReduction(String medicationName, int quantity) {
//        try {
//            repository.confirmStockReduction(medicationName, quantity);
//        } catch (SQLException e) {
//            throw new RuntimeException("Eroare la confirmarea reducerii de stoc", e);
//        }
//    }
//
//    public void cancelReservation(String medicationName, int quantity) {
//        try {
//            repository.cancelReservation(medicationName, quantity);
//        } catch (SQLException e) {
//            throw new RuntimeException("Eroare la anularea rezervării", e);
//        }
//    }
//
//    public int getAvailableStock(String medicationName) {
//        Medication med = findMedicationByName(medicationName);
//        return med != null ? med.getAvailability() - med.getReserved() : 0;
//    }

    public void refreshData() {
        repository.loadData();
    }
}