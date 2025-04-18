package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.smart.entities.User;



public interface UserRepository extends JpaRepository<User, Integer> {
	
	@Query("select u from User u where u.email=:email")
	public User getUserByUserName(@Param("email") String email);
	
	@Transactional  // Required for modifying queries
	@Modifying      // Required for update/delete
	@Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
	void updatePasswordByEmail(@Param("password") String password, @Param("email") String email);
	

}
