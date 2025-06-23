package com.Invnmgmt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Invnmgmt.models.Category;

public interface CategoryRepository  extends JpaRepository<Category, Long>{

}
