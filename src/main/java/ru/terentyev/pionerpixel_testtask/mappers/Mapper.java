package ru.terentyev.pionerpixel_testtask.mappers;

import org.springframework.stereotype.Component;
import ru.terentyev.pionerpixel_testtask.models.Account;
import ru.terentyev.pionerpixel_testtask.models.EmailData;
import ru.terentyev.pionerpixel_testtask.models.PhoneData;
import ru.terentyev.pionerpixel_testtask.models.User;
import ru.terentyev.pionerpixel_testtask.models.dto.AccountDTO;
import ru.terentyev.pionerpixel_testtask.models.dto.EmailDTO;
import ru.terentyev.pionerpixel_testtask.models.dto.PhoneDTO;
import ru.terentyev.pionerpixel_testtask.models.dto.UserDTO;

import java.util.stream.Collectors;


@Component
public class Mapper {

    public UserDTO toUserDto(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDto = new UserDTO();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setDateOfBirth(user.getDateOfBirth());
        if (user.getAccount() != null) {
            userDto.setAccount(toDto(user.getAccount()));
        }
        if (user.getEmails() != null) {
            userDto.setEmails(user.getEmails().stream()
                    .map(this::toDto)
                    .collect(Collectors.toSet()));
        }
        if (user.getPhones() != null) {
            userDto.setPhones(user.getPhones().stream()
                    .map(this::toDto)
                    .collect(Collectors.toSet()));
        }
        return userDto;
    }

    public EmailDTO toDto(EmailData emailData) {
        if (emailData == null) {
            return null;
        }
        EmailDTO emailDto = new EmailDTO();
        emailDto.setId(emailData.getId());
        emailDto.setEmail(emailData.getEmail());
        return emailDto;
    }

    public PhoneDTO toDto(PhoneData phoneData) {
        if (phoneData == null) {
            return null;
        }
        PhoneDTO phoneDto = new PhoneDTO();
        phoneDto.setId(phoneData.getId());
        phoneDto.setPhone(phoneData.getPhone());
        return phoneDto;
    }

    public AccountDTO toDto(Account account) {
        if (account == null) {
            return null;
        }
        AccountDTO accountDto = new AccountDTO();
        accountDto.setId(account.getId());
        accountDto.setBalance(account.getBalance());
        return accountDto;
    }
}
