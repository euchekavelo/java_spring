package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.logging.annotation.InfoLogs;
import com.example.MyBookShopApp.data.ResourceStorage;
import com.example.MyBookShopApp.dto.*;
import com.example.MyBookShopApp.exception.NotFoundException;
import com.example.MyBookShopApp.exception.RecordExistException;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

@Controller
@InfoLogs
public class BooksController {

    private final BookService bookService;
    private final ResourceStorage storage;

    @Autowired
    public BooksController(BookService bookService, ResourceStorage storage) {
        this.bookService = bookService;
        this.storage = storage;
    }

    @ModelAttribute("searchWordDto")
    public SearchWordDto searchWordDto() {
        return new SearchWordDto();
    }

    @GetMapping("/books/{slug}")
    public String bookPage(@PathVariable String slug, Model model) throws NotFoundException {
        Book book = bookService.findBookBySlug(slug);
        model.addAttribute("slugBook", book);
        model.addAttribute("bookRating", bookService.getRoundedAverageScore(slug));
        model.addAttribute("numberOfRating", bookService.getNumberOfBookRating(slug));
        model.addAttribute("ratingMap", bookService.getQuantitativeStatisticsRatingByBookSlug(slug));
        model.addAttribute("bookReview", new BookReviewDto());

        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userDetails.equals("anonymousUser") ? "/books/slug" : "/books/slugmy";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bookRating")
    @ResponseBody
    public ContactConfirmationResponse saveBookRating(@RequestBody BookRatingDto bookRatingDto) {
        bookService.saveBookRating(bookRatingDto.getBookId(), bookRatingDto.getValue());
        ContactConfirmationResponse contactConfirmationResponse = new ContactConfirmationResponse();
        contactConfirmationResponse.setResult("true");
        return contactConfirmationResponse;
    }

    @PostMapping("/books/{slug}/img/save")
    public String saveNewBookImage(@RequestParam("file") MultipartFile file, @PathVariable("slug") String slug)
            throws IOException, NotFoundException {

        String savePath = storage.saveNewBookImage(file, slug);
        Book bookToUpdate = bookService.findBookBySlug(slug);
        bookToUpdate.setImage(savePath);
        bookService.save(bookToUpdate);

        return ("redirect:/books/" + slug);
    }

    @GetMapping("/books/download/{hash}")
    public ResponseEntity<ByteArrayResource> bookFile(@PathVariable("hash") String hash) throws IOException {

        Path path = storage.getBookFilePath(hash);
        Logger.getLogger(this.getClass().getSimpleName()).info("book file path: " + path);

        MediaType mediaType = storage.getBookFileMime(hash);
        Logger.getLogger(this.getClass().getSimpleName()).info("book file mime type: " + mediaType);

        byte[] data = storage.getBookFileByteArray(hash);
        Logger.getLogger(this.getClass().getSimpleName()).info("book file data len: " + data.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + path.getFileName().toString())
                .contentType(mediaType)
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bookReview")
    @ResponseBody
    public ContactConfirmationResponse addBookReview(@RequestBody BookReviewDto bookReviewDto)
            throws NotFoundException, RecordExistException {
        bookService.saveBookReview(bookReviewDto.getBookId(), bookReviewDto.getText());
        ContactConfirmationResponse contactConfirmationResponse = new ContactConfirmationResponse();
        contactConfirmationResponse.setResult("true");
        return contactConfirmationResponse;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/rateBookReview")
    @ResponseBody
    public ContactConfirmationResponse rateBookReview(@RequestBody BookReviewLikeDto bookReviewLikeDto)
            throws NotFoundException {

        bookService.saveBookReviewLike(bookReviewLikeDto.getValue(), bookReviewLikeDto.getReviewId());
        ContactConfirmationResponse contactConfirmationResponse = new ContactConfirmationResponse();
        contactConfirmationResponse.setResult("true");
        return contactConfirmationResponse;
    }

    @PostMapping("/changeBookStatus")
    @ResponseBody
    public ContactConfirmationResponse handleChangeBookStatus(@RequestBody ChangeBookStatusDto changeBookStatusDto,
                   @CookieValue(name = "postponedBookIds",required = false) String postponedBookIds,
                   @CookieValue(name = "cartBookIds", required = false) String cartBookIds,
                   HttpServletRequest request, HttpServletResponse response) {

        bookService.fillCookiesBasedOnStatus(changeBookStatusDto, postponedBookIds, cartBookIds, request, response);
        ContactConfirmationResponse contactConfirmationResponse = new ContactConfirmationResponse();
        contactConfirmationResponse.setResult("true");
        return contactConfirmationResponse;
    }
}
