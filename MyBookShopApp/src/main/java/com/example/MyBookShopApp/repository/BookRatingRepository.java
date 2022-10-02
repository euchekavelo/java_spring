package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.dto.AssessmentDto;
import com.example.MyBookShopApp.model.BookRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRatingRepository extends JpaRepository<BookRating, Integer> {

    @Query(value = "SELECT ROUND(AVG(br.assessment)) FROM book_rating br\n" +
                   "WHERE br.book_id = (SELECT b.id FROM books b WHERE b.slug = :bookSlug)", nativeQuery = true)
    Optional<Integer> roundedAverageAssessment(@Param("bookSlug") String bookSlug);

    Optional<BookRating> getBookRatingByUserIdAndBookId(Integer userId, Integer bookId);

    @Query(value = "SELECT COUNT(*) FROM book_rating br\n" +
            "WHERE br.book_id = (SELECT b.id FROM books b where b.slug = :bookSlug)", nativeQuery = true)
    Optional<Integer> getNumberOfBookRating(@Param("bookSlug") String bookSlug);

    @Query(value = "SELECT COUNT(*) AS count, br.assessment AS assessment " +
            "FROM book_rating br WHERE br.book_id = (SELECT b.id FROM books b WHERE b.slug = :bookSlug) " +
            "GROUP BY br.assessment", nativeQuery = true)
    List<AssessmentDto> getQuantitativeStatisticsRatingByBookSlug(@Param("bookSlug") String bookSlug);
}
