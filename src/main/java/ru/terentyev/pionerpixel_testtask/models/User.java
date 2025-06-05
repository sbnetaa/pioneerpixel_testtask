package ru.terentyev.pionerpixel_testtask.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Document(indexName = "users")
public class User extends AbstractEntity{

    @Column(name = "name", length = 500)
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    @Column(name = "date_of_birth")
    @Field(type = FieldType.Date)
    private LocalDate dateOfBirth;
    @Column(name = "password", length = 500)
    @Field(type = FieldType.Keyword, index = false)
    private String password;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Account account;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Field(type = FieldType.Nested)
    private Set<EmailData> emails = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Field(type = FieldType.Nested)
    private Set<PhoneData> phones = new HashSet<>();

    public void setAccount(Account account) {
        if (account == null) {
            if (this.account != null) {
                this.account.setUser(null);
            }
        } else {
            account.setUser(this);
        }
        this.account = account;
    }

    public void addEmail(EmailData email) {
        emails.add(email);
        email.setUser(this);
    }

    public void removeEmail(EmailData email) {
        emails.remove(email);
        email.setUser(null);
    }

    public void addPhone(PhoneData phone) {
        phones.add(phone);
        phone.setUser(this);
    }

    public void removePhone(PhoneData phone) {
        phones.remove(phone);
        phone.setUser(null);
    }
}