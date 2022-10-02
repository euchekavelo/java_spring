package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.model.Genre;
import com.example.MyBookShopApp.repository.BookRepository;
import com.example.MyBookShopApp.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenrePageService {

    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    @Autowired
    public GenrePageService(GenreRepository genreRepository, BookRepository bookRepository) {
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
    }

    public List<Genre> getGenreList() {
        return genreRepository.findGenresByParentIsNull();
    }

    public Optional<Genre> getGenreBySlug(String genreSlug) {
        return genreRepository.findGenreBySlug(genreSlug);
    }

    public List<Book> getBooksByGenreId(Integer genreId, Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);
        return bookRepository.getBooksByGenreId(genreId, nextPage);
    }
}
