package com.example.do_an_java.repository;

import com.example.do_an_java.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findFirstByBooking_MaCtDatPhongOrderByIdDesc(Integer bookingId);

    List<Payment> findByStatusOrderByCreatedAtDesc(String status);

    List<Payment> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    List<Payment> findAllByOrderByCreatedAtDesc();
}
