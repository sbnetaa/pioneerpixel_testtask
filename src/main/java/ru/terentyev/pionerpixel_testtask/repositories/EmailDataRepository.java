package ru.terentyev.pionerpixel_testtask.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.terentyev.pionerpixel_testtask.models.EmailData;

import java.util.Optional;

@Repository
public interface EmailDataRepository extends JpaRepository<EmailData, Long> {
    Optional<EmailData> findByEmail(String email);
    boolean existsByEmail(String email);
}