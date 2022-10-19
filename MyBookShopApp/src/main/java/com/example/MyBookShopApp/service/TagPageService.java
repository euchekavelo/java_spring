package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.model.Tag;
import com.example.MyBookShopApp.repository.BookRepository;
import com.example.MyBookShopApp.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@DebugLogs
public class TagPageService {

    private final TagRepository tagRepository;
    private final BookRepository bookRepository;

    @Autowired
    public TagPageService(TagRepository tagRepository, BookRepository bookRepository) {
        this.tagRepository = tagRepository;
        this.bookRepository = bookRepository;
    }

    public Optional<Tag> getTagBySlug(String tagSlug) {
        return tagRepository.findTagBySlug(tagSlug);
    }

    public List<Book> getBooksByTagSlug(String tagSlug, Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);
        return bookRepository.findBooksByTagList_Slug(tagSlug, nextPage);
    }

    public List<Book> getBooksByTagId(Integer tagId, Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);
        return bookRepository.findBooksByTagList_Id(tagId, nextPage);
    }
}
