package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.logging.annotation.InfoLogs;
import com.example.MyBookShopApp.dto.BooksPageDto;
import com.example.MyBookShopApp.model.Genre;
import com.example.MyBookShopApp.service.GenrePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@InfoLogs
public class GenrePageController {

    private final GenrePageService genrePageService;

    @Autowired
    public GenrePageController(GenrePageService genrePageService) {
        this.genrePageService = genrePageService;
    }

    @GetMapping("/genres")
    public String genrePage(Model model) {
        model.addAttribute("genreList", genrePageService.getGenreList());
        return "genres/index";
    }

    @GetMapping("/genres/{genreSlug}")
    public String genreSlugPage(@PathVariable String genreSlug, Model model) {
        Optional<Genre> optionalGenre = genrePageService.getGenreBySlug(genreSlug);
        optionalGenre.ifPresent(value -> {
            model.addAttribute("genre", value);
            model.addAttribute("bookList", genrePageService.getBooksByGenreId(value.getId(), 0, 20));
        });
        return "genres/slug";
    }

    @GetMapping("/books/genre/{genreId}")
    @ResponseBody
    public BooksPageDto getNextTagPage(@RequestParam("offset") Integer offset,
                                       @RequestParam("limit") Integer limit,
                                       @PathVariable Integer genreId) {
        return new BooksPageDto(genrePageService.getBooksByGenreId(genreId, offset, limit));
    }
}
