package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.exception.NegativeBalanceException;
import com.example.MyBookShopApp.model.BalanceTransaction;
import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.BalanceTransactionRepository;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.service.BookstoreUserAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class BookShopCartService {

    private final BookService bookService;
    private final UserRepository userRepository;
    private final BookstoreUserAuthorization bookstoreUserAuthorization;
    private final BalanceTransactionRepository balanceTransactionRepository;

    @Autowired
    public BookShopCartService(BookService bookService, UserRepository userRepository,
                               BookstoreUserAuthorization bookstoreUserAuthorization,
                               BalanceTransactionRepository balanceTransactionRepository) {
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.bookstoreUserAuthorization = bookstoreUserAuthorization;
        this.balanceTransactionRepository = balanceTransactionRepository;
    }

    public List<Book> getBookListFromCookieString(String cartBookIds) {
        cartBookIds = cartBookIds.startsWith("/") ? cartBookIds.substring(1) : cartBookIds;
        cartBookIds = cartBookIds.endsWith("/") ? cartBookIds.substring(0, cartBookIds.length() - 1) : cartBookIds;
        int[] bookIds = Arrays.stream(cartBookIds.split("/")).mapToInt(Integer::valueOf).toArray();
        return bookService.getBookListByIdIn(bookIds);
    }

    @Transactional
    public void buyBooksFromCart(List<Book> bookList) throws NegativeBalanceException {
        double totalAmount = bookList.stream().mapToDouble(Book::discountPrice).sum();
        User currentUser = bookstoreUserAuthorization.getCurrentUser();
        double currentUserBalance = currentUser.getBalance();

        if (currentUserBalance - totalAmount < 0) {
            throw new NegativeBalanceException("There is not enough money to buy. Top up your balance.");
        } else {
            currentUser.setBalance(currentUserBalance - totalAmount);
            userRepository.save(currentUser);

            BalanceTransaction balanceTransaction = new BalanceTransaction();
            balanceTransaction.setTime(LocalDateTime.now());
            balanceTransaction.setValue(-totalAmount);
            balanceTransaction.setUser(currentUser);
            balanceTransaction.setDescription("Покупка книг");
            balanceTransaction.setBookList(bookList);
            balanceTransactionRepository.save(balanceTransaction);
        }
    }
}
