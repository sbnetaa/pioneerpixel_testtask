package ru.terentyev.pionerpixel_testtask.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.terentyev.pionerpixel_testtask.exceptions.BadRequestException;
import ru.terentyev.pionerpixel_testtask.exceptions.ResourceNotFoundException;
import ru.terentyev.pionerpixel_testtask.models.Account;
import ru.terentyev.pionerpixel_testtask.models.User;
import ru.terentyev.pionerpixel_testtask.repositories.AccountRepository;
import ru.terentyev.pionerpixel_testtask.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private User senderUser;
    private User recipientUser;
    private Account senderAccount;
    private Account recipientAccount;

    @BeforeEach
    void setUp() {
        senderUser = new User();
        senderUser.setId(1L);
        senderAccount = new Account();
        senderAccount.setId(10L);
        senderAccount.setBalance(new BigDecimal("1000.00"));
        senderUser.setAccount(senderAccount);
        senderAccount.setUser(senderUser);
        recipientUser = new User();
        recipientUser.setId(2L);
        recipientAccount = new Account();
        recipientAccount.setId(20L);
        recipientAccount.setBalance(new BigDecimal("500.00"));
        recipientUser.setAccount(recipientAccount);
        recipientAccount.setUser(recipientUser);
    }
    @Test
    @DisplayName("Should successfully transfer money between users")
    void shouldSuccessfullyTransferMoneyBetweenUsers() {
        BigDecimal transferAmount = new BigDecimal("100.00");
        when(userRepository.findById(senderUser.getId())).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(recipientUser.getId())).thenReturn(Optional.of(recipientUser));
        userService.transferMoney(senderUser.getId(), recipientUser.getId(), transferAmount);
        assertEquals(new BigDecimal("900.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), recipientAccount.getBalance());
        verify(accountRepository, times(1)).save(senderAccount);
        verify(accountRepository, times(1)).save(recipientAccount);
    }
    @Test
    @DisplayName("Should throw BadRequestException when transfer amount is zero")
    void shouldThrowBadRequestExceptionWhenTransferAmountIsZero() {
        BigDecimal transferAmount = BigDecimal.ZERO;
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            userService.transferMoney(senderUser.getId(), recipientUser.getId(), transferAmount);
        });
        assertTrue(thrown.getMessage().contains("Transfer amount must be positive."));
        verify(userRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw BadRequestException when transfer amount is negative")
    void shouldThrowBadRequestExceptionWhenTransferAmountIsNegative() {
        BigDecimal transferAmount = new BigDecimal("-50.00");
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            userService.transferMoney(senderUser.getId(), recipientUser.getId(), transferAmount);
        });
        assertTrue(thrown.getMessage().contains("Transfer amount must be positive."));
        verify(userRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw BadRequestException when transferring to the same user")
    void shouldThrowBadRequestExceptionWhenTransferringToSameUser() {
        BigDecimal transferAmount = new BigDecimal("100.00");
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            userService.transferMoney(senderUser.getId(), senderUser.getId(), transferAmount);
        });
        assertTrue(thrown.getMessage().contains("Cannot transfer money to the same user."));
        verify(userRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw ResourceNotFoundException when sender user not found")
    void shouldThrowResourceNotFoundExceptionWhenSenderUserNotFound() {
        BigDecimal transferAmount = new BigDecimal("100.00");
        when(userRepository.findById(senderUser.getId())).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.transferMoney(senderUser.getId(), recipientUser.getId(), transferAmount);
        });
        assertTrue(thrown.getMessage().contains("Sender user not found with id: " + senderUser.getId()));
        verify(userRepository, times(1)).findById(senderUser.getId());
        verify(userRepository, never()).findById(recipientUser.getId());
        verify(accountRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw ResourceNotFoundException when recipient user not found")
    void shouldThrowResourceNotFoundExceptionWhenRecipientUserNotFound() {
        BigDecimal transferAmount = new BigDecimal("100.00");
        when(userRepository.findById(senderUser.getId())).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(recipientUser.getId())).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.transferMoney(senderUser.getId(), recipientUser.getId(), transferAmount);
        });
        assertTrue(thrown.getMessage().contains("Recipient user not found with id: " + recipientUser.getId()));
        verify(userRepository, times(1)).findById(senderUser.getId());
        verify(userRepository, times(1)).findById(recipientUser.getId());
        verify(accountRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw BadRequestException when insufficient balance")
    void shouldThrowBadRequestExceptionWhenInsufficientBalance() {
        BigDecimal transferAmount = new BigDecimal("1500.00");
        when(userRepository.findById(senderUser.getId())).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(recipientUser.getId())).thenReturn(Optional.of(recipientUser));
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            userService.transferMoney(senderUser.getId(), recipientUser.getId(), transferAmount);
        });
        assertTrue(thrown.getMessage().contains("Insufficient balance for transfer."));
        verify(userRepository, times(1)).findById(senderUser.getId());
        verify(userRepository, times(1)).findById(recipientUser.getId());
        verify(accountRepository, never()).save(any());
    }
}
