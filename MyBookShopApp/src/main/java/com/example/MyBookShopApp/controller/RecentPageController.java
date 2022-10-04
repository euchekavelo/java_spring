package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.dto.BooksPageDto;
import com.example.MyBookShopApp.service.RecentPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/books/recent")
public class RecentPageController {

    private final RecentPageService recentPageService;

    @Autowired
    public RecentPageController(RecentPageService recentPageService) {
        this.recentPageService = recentPageService;
    }

    @GetMapping
    public String recentPage() {
        return "/books/recent";
    }

    @GetMapping(params = {"offset", "limit"})
    @ResponseBody
    public BooksPageDto getNewBooksPage(@RequestParam("offset") Integer offset,
                                        @RequestParam("limit") Integer limit) {
        return new BooksPageDto(recentPageService.getNewBooksOrderByPubDateDesc(offset,limit));
    }

    @ModelAttribute("newBooks")
    public List<Book> listOfRecentBooks() {
        LocalDate currentDate = LocalDate.now();
        String to = currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String from = currentDate.minusMonths(1L).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        return recentPageService.getSortedNewBooksByPubDateBetweenOrderByPubDateDesc(from, to, 0, 20);
    }

    @GetMapping(params = {"from", "to", "offset", "limit"})
    @ResponseBody
    public BooksPageDto getSortedNewBooksPage(@RequestParam("from") String from,
                                            @RequestParam("to") String to,
                                            @RequestParam("offset") Integer offset,
                                            @RequestParam("limit") Integer limit) {

        return new BooksPageDto(recentPageService.getSortedNewBooksByPubDateBetweenOrderByPubDateDesc(from, to,
                offset, limit));
    }
}
