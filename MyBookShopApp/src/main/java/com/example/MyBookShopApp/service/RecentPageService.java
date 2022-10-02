package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RecentPageService {

    private final BookRepository bookRepository;

    @Autowired
    public RecentPageService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getNewBooksOrderByPubDateDesc(Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);
        return bookRepository.findBooksByOrderByPubDateDesc(nextPage);
    }

    public List<Book> getSortedNewBooksByPubDateBetweenOrderByPubDateDesc(String from, String to,
                                                                          Integer offset, Integer limit) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDateFrom = LocalDate.parse(from, format);
        LocalDate localDateTo = LocalDate.parse(to, format);

        Date localDateFromNewFormat = Date.valueOf(localDateFrom);
        Date localDateToNewFormat = Date.valueOf(localDateTo);
        Pageable nextSortedPage = PageRequest.of(offset, limit);

        return bookRepository.findBooksByPubDateBetweenOrderByPubDateDesc(localDateFromNewFormat,
                localDateToNewFormat, nextSortedPage);
    }
}
