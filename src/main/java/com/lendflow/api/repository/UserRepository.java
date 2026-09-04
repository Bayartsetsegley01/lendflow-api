// src/main/java/com/lendflow/api/repository/UserRepository.java
package com.lendflow.api.repository;

import com.lendflow.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Login хийхэд, мөн бүртгүүлэхэд имэйл давхардсан эсэхийг шалгахад ашиглана
    Optional<User> findByEmail(String email);

    // Login-д ашиглана — тухайн имэйлтэй хэрэглэгч бүртгэлтэй эсэхийг шалгах
    boolean existsByEmail(String email);
}