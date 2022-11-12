package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.exception.NegativeBalanceException;
import com.example.MyBookShopApp.logging.annotation.InfoLogs;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.service.BookShopCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@InfoLogs
public class BookshopCartController {

    @ModelAttribute(name = "bookCart")
    public List<Book> bookCart() {
        return new ArrayList<>();
    }
    private final BookShopCartService bookShopCartService;

    @Autowired
    public BookshopCartController(BookShopCartService bookShopCartService) {
        this.bookShopCartService = bookShopCartService;
    }

    @GetMapping("/cart")
    public String handleCartRequest(@CookieValue(value = "cartBookIds", required = false) String cartBookIds,
                                    Model model) {
        if (cartBookIds == null || cartBookIds.equals("")) {
            model.addAttribute("isCartEmpty", true);
        } else {
            model.addAttribute("isCartEmpty", false);
            List<Book> booksFromCookieSlugs = bookShopCartService.getBookListFromCookieString(cartBookIds);
            model.addAttribute("totalAmount", booksFromCookieSlugs.stream().mapToDouble(Book::discountPrice).sum());
            model.addAttribute("bookCart", booksFromCookieSlugs);
        }

        return "cart";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/buy")
    public String handleBuy(@CookieValue(value = "cartBookIds", required = false) String cartBookIds,
                            HttpServletResponse httpServletResponse)
            throws NegativeBalanceException {

        if (cartBookIds != null && !cartBookIds.equals("")) {
            List<Book> booksFromCookieSlugs = bookShopCartService.getBookListFromCookieString(cartBookIds);
            bookShopCartService.buyBooksFromCart(booksFromCookieSlugs);
            Cookie cookie = new Cookie("cartBookIds", "");
            httpServletResponse.addCookie(cookie);
        }

        return "redirect:/cart";
    }
}
