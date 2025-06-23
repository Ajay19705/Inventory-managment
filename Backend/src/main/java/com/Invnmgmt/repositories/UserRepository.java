package com.Invnmgmt.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Invnmgmt.enums.UserRole;
import com.Invnmgmt.models.User;

public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByEmail(String email);
	List<User> findByRole(UserRole role);
	boolean existsByEmail(String email);
}
