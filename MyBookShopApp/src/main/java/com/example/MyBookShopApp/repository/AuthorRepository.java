package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {

    Optional<Author> getAuthorBySlug(String authorSlug);
}
