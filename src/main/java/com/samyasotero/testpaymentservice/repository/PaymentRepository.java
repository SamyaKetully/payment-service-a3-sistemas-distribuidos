package com.samyasotero.testpaymentservice.repository;

import com.samyasotero.testpaymentservice.model.Payment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentRepository {

    void save(Payment payment);

    Payment findById(Long id);

    List<Payment> findAll();

    void update(Payment payment);

    void deleteById(Long id);
}
