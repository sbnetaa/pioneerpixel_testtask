package ru.terentyev.pionerpixel_testtask.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.terentyev.pionerpixel_testtask.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmails_Email(String email);
    Optional<User> findByPhones_Phone(String phone);
}
