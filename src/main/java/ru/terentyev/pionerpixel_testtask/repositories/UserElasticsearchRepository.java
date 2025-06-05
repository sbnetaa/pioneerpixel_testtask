package ru.terentyev.pionerpixel_testtask.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import ru.terentyev.pionerpixel_testtask.models.User;

@Repository
public interface UserElasticsearchRepository extends ElasticsearchRepository<User, Long> {}
