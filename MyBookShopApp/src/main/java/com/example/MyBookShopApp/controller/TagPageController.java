package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.dto.BooksPageDto;
import com.example.MyBookShopApp.model.Tag;
import com.example.MyBookShopApp.service.TagPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class TagPageController {

    private final TagPageService tagPageService;

    @Autowired
    public TagPageController(TagPageService tagPageService) {
        this.tagPageService = tagPageService;
    }

    @GetMapping("/tags/{tagSlug}")
    public String tagPage(@PathVariable String tagSlug, Model model) {
        Optional<Tag> optionalTag = tagPageService.getTagBySlug(tagSlug);
        optionalTag.ifPresent(value -> {
            model.addAttribute("bookList", tagPageService.getBooksByTagSlug(tagSlug, 0, 20));
            model.addAttribute("tagId", value.getId());
            model.addAttribute("tagTitle", value.getTitle());
        });
        return "/tags/index";
    }

    @GetMapping("/books/tag/{tagId}")
    @ResponseBody
    public BooksPageDto getNextTagPage(@RequestParam("offset") Integer offset,
                                       @RequestParam("limit") Integer limit,
                                       @PathVariable Integer tagId) {
        return new BooksPageDto(tagPageService.getBooksByTagId(tagId, offset, limit));
    }
}
