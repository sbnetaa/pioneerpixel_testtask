package ru.terentyev.pionerpixel_testtask.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.terentyev.pionerpixel_testtask.models.dto.UpdateUserRequest;
import ru.terentyev.pionerpixel_testtask.models.dto.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface UserService {
    Optional<UserDTO> findById(Long id);
    Page<UserDTO> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, Pageable pageable);
    UserDTO updateUser(Long userId, UpdateUserRequest updateRequest);
    void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount);
}
