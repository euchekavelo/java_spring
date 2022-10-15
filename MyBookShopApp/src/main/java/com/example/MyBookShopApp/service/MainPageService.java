package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.model.Tag;
import com.example.MyBookShopApp.repository.BookRepository;
import com.example.MyBookShopApp.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@DebugLogs
public class MainPageService {

    private final BookRepository bookRepository;
    private final TagRepository tagRepository;
    private final BooksRatingAndPopularityService booksRatingAndPopularityService;

    @Autowired
    public MainPageService(BookRepository bookRepository, TagRepository tagRepository,
                           BooksRatingAndPopularityService booksRatingAndPopularityService) {
        this.bookRepository = bookRepository;
        this.tagRepository = tagRepository;
        this.booksRatingAndPopularityService = booksRatingAndPopularityService;
    }

    public List<Book> getPageOfRecommendedBooks(HttpServletRequest request, Integer offset, Integer limit) {
        Set<Integer> bookIds = new HashSet<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("cartBookIds")) {
                    String cookieCartString = cookie.getValue();
                    Set<Integer> setCartBookIds = new HashSet<>();
                    if (!cookieCartString.isEmpty()) {
                        setCartBookIds = Arrays.stream(cookieCartString.split("/"))
                                .mapToInt(Integer::valueOf)
                                .boxed()
                                .collect(Collectors.toSet());
                    }

                    bookIds.addAll(setCartBookIds);
                } else if (cookie.getName().equals("postponedBookIds")) {
                    String cookiePostponedString = cookie.getValue();
                    Set<Integer> setPostponedBookIds = new HashSet<>();
                    if (!cookiePostponedString.isEmpty()) {
                        setPostponedBookIds = Arrays.stream(cookiePostponedString.split("/"))
                                .mapToInt(Integer::valueOf)
                                .boxed()
                                .collect(Collectors.toSet());
                    }

                    bookIds.addAll(setPostponedBookIds);
                }
            }
        }

        Pageable nextPage = PageRequest.of(offset,limit);
        Object currentlyAuthObject = SecurityContextHolder.getContext().getAuthentication();
        if (!(currentlyAuthObject instanceof AnonymousAuthenticationToken) && !bookIds.isEmpty()) {
            return bookRepository.getRecommendedBooksForAuthorizedUserWithCookieStrings(new ArrayList<>(bookIds), nextPage);
        } else
            return bookRepository.getBooksOrderByBookRatingDesc(nextPage);
    }

    public List<Book> getNewBooksOrderByPubDateDesc(Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);
        return bookRepository.findBooksByOrderByPubDateDesc(nextPage);
    }

    public List<Book> getSortedBooksByPopularRating(Integer offset, Integer limit) {
        return booksRatingAndPopularityService.getSortedBooksByPopularRating(offset, limit);
    }

    public List<Tag> getTagList() {
        return tagRepository.findAll();
    }

    public Page<Book> getPageOfSearchResultBooks(String searchWord, Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset,limit);
        return bookRepository.findBookByTitleContaining(searchWord,nextPage);
    }
}
