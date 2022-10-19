package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@DebugLogs
public interface BookReviewRepository extends JpaRepository<BookReview, Integer> {

    Optional<BookReview> getBookReviewById(Integer id);

    Optional<BookReview> getBookReviewByBookIdAndUserId(Integer bookId, Integer userId);
}
