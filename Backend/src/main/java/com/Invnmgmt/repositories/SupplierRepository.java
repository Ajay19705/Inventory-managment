package com.Invnmgmt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Invnmgmt.models.Supplier;

public interface SupplierRepository  extends JpaRepository<Supplier, Long>{

}
