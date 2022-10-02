package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    List<Genre> findGenresByParentIsNull();

    Optional<Genre> findGenreBySlug(String genreSlug);
}
