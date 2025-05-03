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

    public List<Medication> getAllMedications(){
        return repository.getMedications();
    }
}
