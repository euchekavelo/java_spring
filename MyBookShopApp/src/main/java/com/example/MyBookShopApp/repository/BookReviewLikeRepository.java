package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.BookReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookReviewLikeRepository extends JpaRepository<BookReviewLike, Integer> {

    Optional<BookReviewLike> findBookReviewLikeByBookReviewIdAndUserId(Integer bookReviewId, Integer userId);

    @Modifying
    @Query(value = "UPDATE book_review_like SET value = :value " +
                   "WHERE review_id = :reviewId AND user_id = :userId", nativeQuery = true)
    int updateBookReviewLikeRecord(@Param("value") Short value, @Param("reviewId") Integer reviewId,
                                   @Param("userId")Integer userId);
}
