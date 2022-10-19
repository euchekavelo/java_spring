package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.logging.annotation.InfoLogs;
import com.example.MyBookShopApp.dto.BooksPageDto;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.service.BooksRatingAndPopularityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/books/popular")
@InfoLogs
public class PopularPageController {

    private final BooksRatingAndPopularityService booksRatingAndPopularityService;

    @Autowired
    public PopularPageController(BooksRatingAndPopularityService booksRatingAndPopularityService) {
        this.booksRatingAndPopularityService = booksRatingAndPopularityService;
    }

    @GetMapping
    public String popularPage() {
        return "/books/popular";
    }

    @ModelAttribute("bookList")
    public List<Book> bookList() {
        return booksRatingAndPopularityService.getSortedBooksByPopularRating(0, 20);
    }

    @GetMapping(params = {"offset", "limit"})
    @ResponseBody
    public BooksPageDto getPopularBooksPage(@RequestParam("offset") Integer offset,
                                            @RequestParam("limit") Integer limit) {
        return new BooksPageDto(booksRatingAndPopularityService.getSortedBooksByPopularRating(offset, limit));
    }
}
