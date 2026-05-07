package ma.fss.repositories;

import ma.fss.entities.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface PatientRepository extends JpaRepository<Patient,Long> {

    Page<Patient> findByNomContains(String nom, Pageable pageable);

    Patient findByUsername(String username);

    @Query("SELECT DISTINCT r.patient FROM RendezVous r WHERE r.medecin.username = :username AND r.patient.nom LIKE %:nom%")
    Page<Patient> findPatientsByDoctorUsernameAndNomContains(@Param("username") String username, @Param("nom") String nom, Pageable pageable);
}