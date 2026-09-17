package com.vivek.HospitalManagement.Repository;

import com.vivek.HospitalManagement.Entity.ImagingOrder;
import com.vivek.HospitalManagement.Enums.ImagingOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImagingOrderRepository extends JpaRepository<ImagingOrder,Long> {

    Optional<ImagingOrder> findByStatus(ImagingOrderStatus status);

    List<ImagingOrder> findByRequestedByUserEmail(String email);
}
