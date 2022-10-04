package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class BookshopPostponedController {

    private final BookService bookService;

    @ModelAttribute(name = "bookCart")
    public List<Book> bookCart() {
        return new ArrayList<>();
    }

    @Autowired
    public BookshopPostponedController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/postponed")
    public String handleCartRequest(@CookieValue(value = "postponedBookIds", required = false) String postponedBookIds,
                                    Model model) {
        if (postponedBookIds == null || postponedBookIds.equals("")) {
            model.addAttribute("isCartEmpty", true);
        } else {
            model.addAttribute("isCartEmpty", false);
            postponedBookIds = postponedBookIds.startsWith("/") ?
                    postponedBookIds.substring(1) : postponedBookIds;
            postponedBookIds = postponedBookIds.endsWith("/") ?
                    postponedBookIds.substring(0, postponedBookIds.length() - 1) : postponedBookIds;
            int[] bookIds = Arrays.stream(postponedBookIds.split("/")).mapToInt(Integer::valueOf).toArray();
            List<Book> booksFromCookieIds = bookService.getBookListByIdIn(bookIds);
            model.addAttribute("bookCart", booksFromCookieIds);
        }

        return "postponed";
    }
}
