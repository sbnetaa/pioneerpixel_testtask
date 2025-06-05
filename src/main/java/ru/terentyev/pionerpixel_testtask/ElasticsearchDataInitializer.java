package ru.terentyev.pionerpixel_testtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.terentyev.pionerpixel_testtask.models.User;
import ru.terentyev.pionerpixel_testtask.repositories.UserElasticsearchRepository;
import ru.terentyev.pionerpixel_testtask.repositories.UserRepository;

import java.util.List;

@Component
public class ElasticsearchDataInitializer {

    private final UserRepository userRepository;
    private final UserElasticsearchRepository userElasticsearchRepository;

    @Autowired
    public ElasticsearchDataInitializer(UserRepository userRepository, UserElasticsearchRepository userElasticsearchRepository) {
        this.userRepository = userRepository;
        this.userElasticsearchRepository = userElasticsearchRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeElasticsearchData() {
        userElasticsearchRepository.deleteAll();
        List<User> users = userRepository.findAll();
        userElasticsearchRepository.saveAll(users);
        System.out.println("Elasticsearch index 'users' initialized with data from PostgreSQL.");
    }
}
