package com.example.file;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {

}

