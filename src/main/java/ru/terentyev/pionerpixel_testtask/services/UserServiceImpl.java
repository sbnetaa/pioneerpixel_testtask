package ru.terentyev.pionerpixel_testtask.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.terentyev.pionerpixel_testtask.exceptions.BadRequestException;
import ru.terentyev.pionerpixel_testtask.exceptions.ResourceNotFoundException;
import ru.terentyev.pionerpixel_testtask.mappers.Mapper;
import ru.terentyev.pionerpixel_testtask.models.Account;
import ru.terentyev.pionerpixel_testtask.models.EmailData;
import ru.terentyev.pionerpixel_testtask.models.PhoneData;
import ru.terentyev.pionerpixel_testtask.models.User;
import ru.terentyev.pionerpixel_testtask.models.dto.EmailDTO;
import ru.terentyev.pionerpixel_testtask.models.dto.PhoneDTO;
import ru.terentyev.pionerpixel_testtask.models.dto.UpdateUserRequest;
import ru.terentyev.pionerpixel_testtask.models.dto.UserDTO;
import ru.terentyev.pionerpixel_testtask.repositories.AccountRepository;
import ru.terentyev.pionerpixel_testtask.repositories.EmailDataRepository;
import ru.terentyev.pionerpixel_testtask.repositories.PhoneDataRepository;
import ru.terentyev.pionerpixel_testtask.repositories.UserElasticsearchRepository;
import ru.terentyev.pionerpixel_testtask.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;
    private final UserElasticsearchRepository userElasticsearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final Mapper mapper;
    private final Lock accountLock = new ReentrantLock();

    @Autowired
    public UserServiceImpl(UserRepository userRepository
            , AccountRepository accountRepository, EmailDataRepository emailDataRepository
            , PhoneDataRepository phoneDataRepository
            , UserElasticsearchRepository userElasticsearchRepository
            , ElasticsearchOperations elasticsearchOperations, Mapper mapper) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.emailDataRepository = emailDataRepository;
        this.phoneDataRepository = phoneDataRepository;
        this.userElasticsearchRepository = userElasticsearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public Optional<UserDTO> findById(Long id) {
        logger.info("Attempting to find user with ID: {}", id);
        return userRepository.findById(id)
                .map(mapper::toUserDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, Pageable pageable) {
        logger.info("Searching users with filters: dateOfBirth={}, phone={}, name={}, email={}", dateOfBirth, phone, name, email);
        Criteria criteria = new Criteria();
        if (dateOfBirth != null) {
            criteria.and("dateOfBirth").greaterThan(dateOfBirth);
        }
        if (phone != null) {
            criteria.and("phones.phone").is(phone);
        }
        if (name != null) {
            criteria.and("name").startsWith(name);
        }
        if (email != null) {
            criteria.and("emails.email").is(email);
        }
        Query searchQuery = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<User> searchHits = elasticsearchOperations.search(searchQuery, User.class);
        logger.info("Found {} users matching search criteria.", searchHits.getTotalHits());
        SearchPage<User> userSearchPage = SearchHitSupport.searchPageFor(searchHits, pageable);
        List<UserDTO> userDtos = userSearchPage.getContent().stream()
                .map(searchHit -> mapper.toUserDto(searchHit.getContent()))
                .collect(Collectors.toList());
        return new PageImpl<>(userDtos, pageable, userSearchPage.getTotalElements());
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDTO updateUser(Long userId, UpdateUserRequest updateRequest) {
        logger.info("Attempting to update user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (updateRequest.getEmails() != null) {
            updateEmails(user, updateRequest.getEmails());
        }
        if (updateRequest.getPhones() != null) {
            updatePhones(user, updateRequest.getPhones());
        }
        User updatedUser = userRepository.save(user);
        logger.info("User with ID {} updated successfully.", userId);
        return mapper.toUserDto(updatedUser);
    }

    private void updateEmails(User user, Set<EmailDTO> emailDtos) {
        Set<EmailData> currentEmails = new HashSet<>(user.getEmails());
        Set<String> updatedEmailAddresses = emailDtos.stream()
                .map(EmailDTO::getEmail)
                .collect(Collectors.toSet());
        for (EmailData currentEmail : currentEmails) {
            if (!updatedEmailAddresses.contains(currentEmail.getEmail())) {
                user.removeEmail(currentEmail);
                emailDataRepository.delete(currentEmail);
            }
        }
        for (EmailDTO emailDto : emailDtos) {
            Optional<EmailData> existingEmail = user.getEmails().stream()
                    .filter(e -> e.getEmail().equals(emailDto.getEmail()))
                    .findFirst();
            if (existingEmail.isEmpty()) {
                if (emailDataRepository.existsByEmail(emailDto.getEmail())) {
                    throw new BadRequestException("Email address already exists: " + emailDto.getEmail());
                }
                EmailData newEmail = new EmailData();
                newEmail.setEmail(emailDto.getEmail());
                user.addEmail(newEmail);
            }
        }
        if (user.getEmails().isEmpty()) {
            throw new BadRequestException("User must have at least one email address.");
        }
    }

    private void updatePhones(User user, Set<PhoneDTO> phoneDtos) {
        Set<PhoneData> currentPhones = new HashSet<>(user.getPhones());
        Set<String> updatedPhoneNumbers = phoneDtos.stream()
                .map(PhoneDTO::getPhone)
                .collect(Collectors.toSet());
        for (PhoneData currentPhone : currentPhones) {
            if (!updatedPhoneNumbers.contains(currentPhone.getPhone())) {
                user.removePhone(currentPhone);
                phoneDataRepository.delete(currentPhone);
            }
        }
        for (PhoneDTO phoneDto : phoneDtos) {
            Optional<PhoneData> existingPhone = user.getPhones().stream()
                    .filter(p -> p.getPhone().equals(phoneDto.getPhone()))
                    .findFirst();
            if (existingPhone.isEmpty()) {
                if (phoneDataRepository.existsByPhone(phoneDto.getPhone())) {
                    throw new BadRequestException("Phone number already exists: " + phoneDto.getPhone());
                }
                PhoneData newPhone = new PhoneData();
                newPhone.setPhone(phoneDto.getPhone());
                user.addPhone(newPhone);
            }
        }
        if (user.getPhones().isEmpty()) {
            throw new BadRequestException("User must have at least one phone number.");
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount) {
        logger.info("Attempting to transfer {} from user {} to user {}", amount, fromUserId, toUserId);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid transfer amount: {}", amount);
            throw new BadRequestException("Transfer amount must be positive.");
        }
        if (fromUserId.equals(toUserId)) {
            throw new BadRequestException("Cannot transfer money to the same user.");
        }
        accountLock.lock();
        try {
            User fromUser = userRepository.findById(fromUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sender user not found with id: " + fromUserId));
            User toUser = userRepository.findById(toUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found with id: " + toUserId));
            Account fromAccount = fromUser.getAccount();
            Account toAccount = toUser.getAccount();
            if (fromAccount == null) {
                throw new ResourceNotFoundException("Sender user account not found.");
            }
            if (toAccount == null) {
                throw new ResourceNotFoundException("Recipient user account not found.");
            }
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                logger.error("Insufficient balance for user {}. Current balance: {}", fromUserId, fromAccount.getBalance());
                throw new BadRequestException("Insufficient balance for transfer.");
            }
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            logger.info("Successfully transferred {} from user {} to user {}", amount, fromUserId, toUserId);
        } finally {
            accountLock.unlock();
        }
    }
}