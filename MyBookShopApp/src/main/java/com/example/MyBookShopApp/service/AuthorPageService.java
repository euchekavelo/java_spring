package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.Author;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.repository.AuthorRepository;
import com.example.MyBookShopApp.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@DebugLogs
public class AuthorPageService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorPageService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public Map<String, List<Author>> getAuthorsMap() {
        List<Author> authors = authorRepository.findAll();
        return authors.stream().collect(Collectors.groupingBy(author -> author.getName().substring(0, 1)));
    }

    public Optional<Author> getAuthorBySlug(String authorSlug) {
        return authorRepository.getAuthorBySlug(authorSlug);
    }

    public List<Book> getBooksByAuthorIdOrderByTitle(Integer authorId, Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.getBooksByAuthorList_IdOrderByTitle(authorId, nextPage);
    }
}
