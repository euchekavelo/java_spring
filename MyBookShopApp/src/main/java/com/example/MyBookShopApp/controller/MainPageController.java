package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.exception.EmptySearchException;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.model.Tag;
import com.example.MyBookShopApp.service.*;
import com.example.MyBookShopApp.dto.BooksPageDto;
import com.example.MyBookShopApp.dto.SearchWordDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainPageController {

    private final MainPageService mainPageService;

    @Autowired
    public MainPageController(MainPageService mainPageService) {
        this.mainPageService = mainPageService;
    }

    @ModelAttribute("recommendedBooks")
    public List<Book> recommendedBooks(HttpServletRequest request) {
        return mainPageService.getPageOfRecommendedBooks(request, 0, 6);
    }

    @ModelAttribute("newBooks")
    public List<Book> listOfRecentBooks() {
        return mainPageService.getNewBooksOrderByPubDateDesc(0, 6);
    }

    @ModelAttribute("popularBooks")
    public List<Book> listOfPopularBooks() {
        return mainPageService.getSortedBooksByPopularRating(0, 6);
    }

    @ModelAttribute("searchWordDto")
    public SearchWordDto searchWordDto() {
        return new SearchWordDto();
    }

    @ModelAttribute("searchResults")
    public List<Book> searchResults() {
        return new ArrayList<>();
    }

    @ModelAttribute("tagList")
    public List<Tag> tagList() {
        return mainPageService.getTagList();
    }

    @GetMapping
    public String mainPage() {
        return "index";
    }

    @GetMapping("/books/recommended")
    @ResponseBody
    public BooksPageDto getRecommendedBooksPage(HttpServletRequest request, @RequestParam("offset") Integer offset,
                                                @RequestParam("limit") Integer limit) {
        return new BooksPageDto(mainPageService.getPageOfRecommendedBooks(request, offset, limit));
    }

    @GetMapping(value = {"/search", "/search/{searchWord}"})
    public String getSearchResults(@PathVariable(value = "searchWord", required = false) SearchWordDto searchWordDto,
                                   Model model) throws EmptySearchException {
        if (searchWordDto != null) {
            model.addAttribute("searchWordDto", searchWordDto);
            model.addAttribute("searchResults",
                    mainPageService.getPageOfSearchResultBooks(searchWordDto.getExample(), 0, 5).getContent());
            return "/search/index";
        } else {
            throw new EmptySearchException("Поиск по null невозможен");
        }
    }

    @GetMapping("/search/page/{searchWord}")
    @ResponseBody
    public BooksPageDto getNextSearchPage(@RequestParam("offset") Integer offset,
                                          @RequestParam("limit") Integer limit,
                                          @PathVariable(value = "searchWord", required = false) SearchWordDto searchWordDto) {
        return new BooksPageDto(mainPageService.getPageOfSearchResultBooks(searchWordDto.getExample(), offset, limit).getContent());
    }
}
