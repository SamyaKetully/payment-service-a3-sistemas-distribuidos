package com.samyasotero.testpaymentservice.repository;

import com.samyasotero.testpaymentservice.model.Payment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface PaymentRepository {

    Payment findByOrderId(UUID orderId);

    void save(Payment payment);

    Payment findById(UUID id);

    List<Payment> findAll();

    void update(Payment payment);

    void deleteById(UUID id);
}
