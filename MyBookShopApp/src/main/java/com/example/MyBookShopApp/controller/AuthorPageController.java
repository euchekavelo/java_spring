package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.dto.BooksPageDto;
import com.example.MyBookShopApp.dto.SearchWordDto;
import com.example.MyBookShopApp.model.Author;
import com.example.MyBookShopApp.service.AuthorPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthorPageController {

    private final AuthorPageService authorPageService;

    @Autowired
    public AuthorPageController(AuthorPageService authorPageService) {
        this.authorPageService = authorPageService;
    }

    @ModelAttribute("searchWordDto")
    public SearchWordDto searchWordDto(){
        return new SearchWordDto();
    }

    @ModelAttribute("authorsMap")
    public Map<String,List<Author>> authorsMap(){
        return authorPageService.getAuthorsMap();
    }

    @GetMapping("/authors")
    public String authorsPage(){
        return "/authors/index";
    }

    @GetMapping("/authors/{authorSlug}")
    public String authorPersonalPage(@PathVariable String authorSlug, Model model) {
        Optional<Author> authorOptional = authorPageService.getAuthorBySlug(authorSlug);
        authorOptional.ifPresent(value -> {
            int start = value.getDescription().indexOf(".") + 1;
            String shortBio = value.getDescription().substring(0, start);
            String restOfTheBio = value.getDescription().substring(start);
            model.addAttribute("authorId", value.getId());
            model.addAttribute("authorName", value.getName());
            model.addAttribute("authorImage", value.getPhoto());
            model.addAttribute("shortBio", shortBio);
            model.addAttribute("restOfTheBio", restOfTheBio);
            model.addAttribute("authorSlug", authorSlug);
            model.addAttribute("totalBookListSize", value.getBookList().size());
            model.addAttribute("bookList", authorPageService.getBooksByAuthorIdOrderByTitle(value.getId(), 0, 6));
        });
        return "/authors/slug";
    }

    @GetMapping(value = "/books/author/{authorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BooksPageDto getRecommendedBooksPage(@RequestParam("offset") Integer offset,
                                                @RequestParam("limit") Integer limit,
                                                @PathVariable Integer authorId) {
        return new BooksPageDto(authorPageService.getBooksByAuthorIdOrderByTitle(authorId, offset, limit));
    }

    @GetMapping(value = "/books/author/{authorSlug}", produces = MediaType.TEXT_HTML_VALUE)
    public String authorBooksPage(@PathVariable String authorSlug, Model model) {
        Optional<Author> authorOptional = authorPageService.getAuthorBySlug(authorSlug);
        authorOptional.ifPresent(value -> {
            model.addAttribute("authorId", value.getId());
            model.addAttribute("authorName", value.getName());
            model.addAttribute("bookList", authorPageService.getBooksByAuthorIdOrderByTitle(value.getId(), 0, 20));
        });
        return "/books/author";
    }
}
