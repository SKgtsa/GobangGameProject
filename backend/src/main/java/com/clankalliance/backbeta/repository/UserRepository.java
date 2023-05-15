package com.clankalliance.backbeta.repository;

import com.clankalliance.backbeta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,String> {

    @Query(value = "from User u where u.phone=?1")
    Optional<User> findByPhone(String phone);

}
