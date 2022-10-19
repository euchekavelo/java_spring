package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.logging.annotation.InfoLogs;
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
@InfoLogs
public class BookshopCartController {

    @ModelAttribute(name = "bookCart")
    public List<Book> bookCart() {
        return new ArrayList<>();
    }
    private final BookService bookService;

    @Autowired
    public BookshopCartController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/cart")
    public String handleCartRequest(@CookieValue(value = "cartBookIds", required = false) String cartBookIds,
                                    Model model) {
        if (cartBookIds == null || cartBookIds.equals("")) {
            model.addAttribute("isCartEmpty", true);
        } else {
            model.addAttribute("isCartEmpty", false);
            cartBookIds = cartBookIds.startsWith("/") ? cartBookIds.substring(1) : cartBookIds;
            cartBookIds = cartBookIds.endsWith("/") ? cartBookIds.substring(0, cartBookIds.length() - 1) : cartBookIds;
            int[] bookIds = Arrays.stream(cartBookIds.split("/")).mapToInt(Integer::valueOf).toArray();
            List<Book> booksFromCookieSlugs = bookService.getBookListByIdIn(bookIds);
            double oldTotalAmount = booksFromCookieSlugs.stream().mapToDouble(Book::getPriceOld).sum();
            double totalAmount = booksFromCookieSlugs.stream().mapToDouble(Book::discountPrice).sum();
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("oldTotalAmount", oldTotalAmount);
            model.addAttribute("bookCart", booksFromCookieSlugs);
        }

        return "cart";
    }
}
