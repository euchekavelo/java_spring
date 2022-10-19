package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.dto.AssessmentDto;
import com.example.MyBookShopApp.dto.ChangeBookStatusDto;
import com.example.MyBookShopApp.exception.BookstoreApiWrongParameterException;
import com.example.MyBookShopApp.exception.NotFoundException;
import com.example.MyBookShopApp.exception.RecordExistException;
import com.example.MyBookShopApp.model.*;
import com.example.MyBookShopApp.repository.*;
import com.example.MyBookShopApp.security.service.BookstoreUserAuthorization;
import com.example.MyBookShopApp.security.service.BookstoreUserRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@DebugLogs
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewLikeRepository bookReviewLikeRepository;
    private final BookRatingRepository bookRatingRepository;
    private final BookstoreUserRegister bookstoreUserRegister;
    private final BookstoreUserAuthorization bookstoreUserAuthorization;

    @Autowired
    public BookService(BookRepository bookRepository, UserRepository userRepository,
                       BookReviewRepository bookReviewRepository, BookReviewLikeRepository bookReviewLikeRepository,
                       BookRatingRepository bookRatingRepository, BookstoreUserRegister bookstoreUserRegister, BookstoreUserAuthorization bookstoreUserAuthorization) {

        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookReviewRepository = bookReviewRepository;
        this.bookReviewLikeRepository = bookReviewLikeRepository;
        this.bookRatingRepository = bookRatingRepository;
        this.bookstoreUserRegister = bookstoreUserRegister;
        this.bookstoreUserAuthorization = bookstoreUserAuthorization;
    }

    public List<Book> getBooksByTitle(String title) throws BookstoreApiWrongParameterException {
        if (title.length() <= 1){
            throw new BookstoreApiWrongParameterException("Wrong values passed to one or more parameters");
        }else {
            List<Book> data = bookRepository.findBooksByTitleContaining(title);
            if (data.size() > 0){
                return data;
            }else {
                throw new BookstoreApiWrongParameterException("No data found with specified parameters...");
            }
        }
    }

    public List<Book> getBooksWithPriceBetween(Integer min, Integer max){
        return bookRepository.findBooksByPriceOldBetween(min,max);
    }

    public List<Book> getBooksWithMaxPrice(){
        return bookRepository.getBooksWithMaxDiscount();
    }

    public List<Book> getBestsellers(){
        return bookRepository.getBestsellers();
    }

    public Book findBookBySlug(String slug) throws NotFoundException {
        Optional<Book> optionalBook = bookRepository.findBookBySlug(slug);
        if (optionalBook.isPresent()) {
            return optionalBook.get();
        } else {
            throw new NotFoundException("The book with the criteria '" + slug + "' was not found.");
        }
    }

    public void save(Book bookToUpdate) {
        bookRepository.save(bookToUpdate);
    }

    public Integer getRoundedAverageScore(String bookSlug) {
        Optional<Integer> roundedAverageAssessment = bookRatingRepository.roundedAverageAssessment(bookSlug);
        return roundedAverageAssessment.orElse(0);
    }

    public void saveBookRating(Integer bookId, Integer value) {
        User currentUser = bookstoreUserAuthorization.getCurrentUser();
        Optional<BookRating> optionalBookRating =
                bookRatingRepository.getBookRatingByUserIdAndBookId(currentUser.getId(), bookId);
        BookRating bookRating;
        if (!optionalBookRating.isPresent()) {
            bookRating = new BookRating();
            bookRating.setUserId(currentUser.getId());
            bookRating.setBookId(bookId);
        } else {
            bookRating = optionalBookRating.get();
        }
        bookRating.setAssessment(value);
        bookRatingRepository.save(bookRating);
    }

    public void saveBookReview(Integer bookId, String text) throws NotFoundException, RecordExistException {
        User currentUser = bookstoreUserAuthorization.getCurrentUser();
        Optional<Book> optionalBook = bookRepository.findBookById(bookId);
        Optional<User> optionalUser = userRepository.findUserById(currentUser.getId());
        if (optionalBook.isPresent() && optionalUser.isPresent()) {
            if (bookReviewRepository.getBookReviewByBookIdAndUserId(bookId, currentUser.getId()).isPresent()) {
                throw new RecordExistException("The current user has left a review before.");
            }
            Book book = optionalBook.get();
            User user = optionalUser.get();
            BookReview bookReview = new BookReview();
            bookReview.setUser(user);
            bookReview.setBook(book);
            bookReview.setText(text);
            bookReview.setTime(LocalDateTime.now());
            bookReviewRepository.save(bookReview);
        } else {
            throw new NotFoundException("The book with the criteria '" + bookId + "' was not found.");
        }
    }

    private void updateOrDeleteBookReviewLikeByValue(BookReviewLike currentBookReviewLike, Integer bookReviewId,
                                                     Integer userId, Short value) {

        if (currentBookReviewLike.getValue() == value) {
            bookReviewLikeRepository.delete(currentBookReviewLike);
        } else {
            bookReviewLikeRepository.updateBookReviewLikeRecord(value, bookReviewId, userId);
        }
    }

    @Transactional
    public void saveBookReviewLike(Short value, Integer bookReviewId) throws NotFoundException {
        User currentUser = bookstoreUserAuthorization.getCurrentUser();
        Optional<BookReviewLike> optionalBookReviewLike =
                bookReviewLikeRepository.findBookReviewLikeByBookReviewIdAndUserId(bookReviewId, currentUser.getId());
        if (optionalBookReviewLike.isPresent()) {
            updateOrDeleteBookReviewLikeByValue(optionalBookReviewLike.get(), bookReviewId, currentUser.getId(), value);
        } else {
            Optional<BookReview> optionalBookReview = bookReviewRepository.getBookReviewById(bookReviewId);
            Optional<User> optionalUser = userRepository.findUserById(currentUser.getId());
            if (optionalBookReview.isPresent() && optionalUser.isPresent()) {
                BookReviewLike bookReviewLike = new BookReviewLike();
                bookReviewLike.setBookReview(optionalBookReview.get());
                bookReviewLike.setUser(optionalUser.get());
                bookReviewLike.setValue(value);
                bookReviewLike.setTime(LocalDateTime.now());
                bookReviewLikeRepository.save(bookReviewLike);
            } else {
                throw new NotFoundException("Failed to save feedback score. The review with id " + bookReviewId +
                        " was not found in the database.");
            }
        }
    }

    public Integer getNumberOfBookRating(String bookSlug) {
        return bookRatingRepository.getNumberOfBookRating(bookSlug).orElse(0);
    }

    public Map<Integer, Integer> getQuantitativeStatisticsRatingByBookSlug(String bookSlug) {
        return bookRatingRepository.getQuantitativeStatisticsRatingByBookSlug(bookSlug).stream()
                .collect(Collectors.toMap(AssessmentDto::getAssessment, AssessmentDto::getCount));
    }

    private void fillBookIdCookies(Integer bookId, String cookieString, String nameCookie, HttpServletResponse response) {
        String stringBookId = bookId.toString();
        if (cookieString == null || cookieString.equals("")) {
            Cookie cookie = new Cookie(nameCookie, stringBookId);
            cookie.setPath("/");
            response.addCookie(cookie);
        } else if (!cookieString.contains(stringBookId)) {
            StringJoiner stringJoiner = new StringJoiner("/");
            stringJoiner.add(cookieString).add(stringBookId);
            Cookie cookie = new Cookie(nameCookie, stringJoiner.toString());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    private void removeBookIdFromCookieString(Integer bookId, String cookieString, String nameCookie,
                                              HttpServletResponse response) {
        String stringBookId = bookId.toString();
        if (cookieString.contains(stringBookId)) {
            ArrayList<String> cookieBooks = new ArrayList<>(Arrays.asList(cookieString.split("/")));
            cookieBooks.remove(stringBookId);
            Cookie cookie = new Cookie(nameCookie, String.join("/", cookieBooks));
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }


    public void fillCookiesBasedOnStatus(ChangeBookStatusDto changeBookStatusDto, String postponedBookIds,
                                          String cartBookIds, HttpServletRequest request, HttpServletResponse response) {
        String changeStatus = changeBookStatusDto.getStatus();
        String refererUrl = request.getHeader("Referer");

        if (refererUrl.contains("/postponed")) {
            if (changeStatus.equals("CART")) {
                fillBookIdCookies(changeBookStatusDto.getBooksIds(), cartBookIds, "cartBookIds", response);
                removeBookIdFromCookieString(changeBookStatusDto.getBooksIds(), postponedBookIds,
                        "postponedBookIds", response);
            } else if (changeStatus.equals("UNLINK")) {
                removeBookIdFromCookieString(changeBookStatusDto.getBooksIds(), postponedBookIds,
                        "postponedBookIds", response);
            }
        } else if (refererUrl.contains("/cart")) {
            if (changeStatus.equals("KEPT")) {
                fillBookIdCookies(changeBookStatusDto.getBooksIds(), postponedBookIds,
                        "postponedBookIds", response);
                removeBookIdFromCookieString(changeBookStatusDto.getBooksIds(), cartBookIds,
                        "cartBookIds", response);
            } else if (changeStatus.equals("UNLINK")) {
                removeBookIdFromCookieString(changeBookStatusDto.getBooksIds(), cartBookIds,
                        "cartBookIds", response);
            }
        } else {
            if (changeStatus.equals("CART")) {
                fillBookIdCookies(changeBookStatusDto.getBooksIds(), cartBookIds, "cartBookIds", response);
            } else if (changeStatus.equals("KEPT")) {
                fillBookIdCookies(changeBookStatusDto.getBooksIds(), postponedBookIds,
                        "postponedBookIds", response);
            }
        }
    }

    public List<Book> getBookListByIdIn(int[] bookIds) {
        return bookRepository.findBooksByIdIn(bookIds);
    }
}
