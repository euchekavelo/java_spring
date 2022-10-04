package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class BooksRatingAndPopularityService {

    private final BookRepository bookRepository;

    @Autowired
    public BooksRatingAndPopularityService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public double calculatePopularityRating(int boughtByUsers, int addedToCartByUsers, int delayedByUsers) {
        return boughtByUsers + 0.7 * addedToCartByUsers + 0.4 * delayedByUsers;
    }

    public List<Book> getSortedBooksByPopularRating(Integer offset, Integer limit) {
        List<Book> bookList = bookRepository.findAll();
        bookList.sort((Comparator.comparingDouble(book -> calculatePopularityRating(book.getBoughtByUsers(),
                book.getAddedToCartByUsers(), book.getDelayedByUsers()))));
        Collections.reverse(bookList);

        Pageable nextPage = PageRequest.of(offset,limit);
        int start = Math.min((int)nextPage.getOffset(), bookList.size());
        int end = Math.min((start + nextPage.getPageSize()), bookList.size());
        Page<Book> page = new PageImpl<>(bookList.subList(start, end), nextPage, bookList.size());

        return page.getContent();
    }
}
