package ru.terentyev.pionerpixel_testtask.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.terentyev.pionerpixel_testtask.exceptions.BadRequestException;
import ru.terentyev.pionerpixel_testtask.exceptions.ResourceNotFoundException;
import ru.terentyev.pionerpixel_testtask.models.UserPrincipal;
import ru.terentyev.pionerpixel_testtask.models.dto.TransferRequest;
import ru.terentyev.pionerpixel_testtask.models.dto.UpdateUserRequest;
import ru.terentyev.pionerpixel_testtask.models.dto.UserDTO;
import ru.terentyev.pionerpixel_testtask.services.UserService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController extends AbstractController{

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateOfBirth,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            Pageable pageable) {
        Page<UserDTO> users = userService.searchUsers(dateOfBirth, phone, name, email, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest updateRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (!id.equals(currentUser.getId())) {
            throw new BadRequestException("You can only update your own profile.");
        }
        UserDTO updatedUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(
            @Valid @RequestBody TransferRequest transferRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.transferMoney(currentUser.getId(), transferRequest.getToUserId(), transferRequest.getAmount());
        return ResponseEntity.ok().build();
    }
}
