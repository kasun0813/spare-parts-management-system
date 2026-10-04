package com.autohublanka.catalog.repository;

import com.autohublanka.catalog.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByMakeIgnoreCaseAndModelIgnoreCaseAndYear(String make, String model, int year);

    List<Vehicle> findAllByOrderByMakeAscModelAscYearDesc();
}
