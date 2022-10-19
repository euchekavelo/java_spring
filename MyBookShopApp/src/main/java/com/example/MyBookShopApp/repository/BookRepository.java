package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
@DebugLogs
public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findBooksByTitleContaining(String bookTitle);

    List<Book> findBooksByPriceOldBetween(Integer min, Integer max);

    @Query("from Book where isBestseller=1")
    List<Book> getBestsellers();

    @Query(value = "SELECT * FROM books WHERE discount = (SELECT MAX(discount) FROM books)", nativeQuery = true)
    List<Book> getBooksWithMaxDiscount();

    Page<Book> findBookByTitleContaining(String bookTitle, Pageable nextPage);

    List<Book> findBooksByOrderByPubDateDesc(Pageable nextPage);

    List<Book> findBooksByPubDateBetweenOrderByPubDateDesc(Date from, Date to, Pageable nextPage);

    List<Book> findBooksByTagList_Slug(String tagSlug, Pageable nextPage);

    List<Book> findBooksByTagList_Id(Integer tagId, Pageable nextPage);

    @Query(value = "WITH RECURSIVE rec as (\n" +
                        "\tSELECT g.id \n" +
                        "\tFROM genres g \n" +
                        "\tWHERE g.id = :genreId \n" +
                        "\tUNION ALL \n" +
                        "\tSELECT g.id \n" +
                        "\tFROM genres g INNER JOIN rec r ON g.parent_id = r.id \n" +
                    ") \n" +
                    "SELECT b.* \n" +
                    "FROM books b INNER JOIN book2genre bg ON b.id = bg.book_id \n" +
                    "LEFT JOIN genres g ON bg.genre_id = g.id \n" +
                    "WHERE g.id IN (SELECT * FROM rec r)", nativeQuery = true)
    List<Book> getBooksByGenreId(@Param("genreId") Integer genreId, Pageable nextPage);

    List<Book> getBooksByAuthorList_IdOrderByTitle(Integer authorId, Pageable nextPage);

    Optional<Book> findBookBySlug(String slug);

    List<Book> findBooksBySlugIn(String[] slugs);

    List<Book> findBooksByIdIn(int[] bookIds);

    Optional<Book> findBookById(Integer id);

    @Query(value = "SELECT\tb.id,\n" +
            "\t\tb.added_to_cart_by_users,\n" +
            "\t\tb.bought_by_users,\n" +
            "\t\tb.delayed_by_users,\n" +
            "\t\tb.description,\n" +
            "\t\tb.image,\n" +
            "\t\tb.is_bestseller,\n" +
            "\t\tb.discount,\n" +
            "\t\tb.price,\n" +
            "\t\tb.pub_date,\n" +
            "\t\tb.slug,\n" +
            "\t\tb.title,\n" +
            "\t\tb.bought_by_users + (0.7 * b.added_to_cart_by_users) + (0.4 * b.delayed_by_users) AS book_rating\n" +
            "\t\n" +
            "FROM\tbooks b \n" +
            "ORDER BY book_rating DESC, pub_date DESC", nativeQuery = true)
    List<Book> getBooksOrderByBookRatingDesc(Pageable nextPage);


    @Query(value = "WITH unique_book_ids_by_tag_ids as (\n" +
            "\tselect distinct tb.book_id\n" +
            "\tfrom tag2book tb \n" +
            "\twhere tb.book_id not in (:bookIds) \n" +
            "\t\tAND tb.tag_id IN (select DISTINCT tb.tag_id from tag2book tb where tb.book_id IN (:bookIds))\n" +
            "),\n" +
            "\n" +
            "unique_book_ids_by_author_ids as (\n" +
            "\tselect distinct ba.book_id\n" +
            "\tfrom book2author ba\n" +
            "\twhere ba.book_id not in (:bookIds) \n" +
            "\t\tAND ba.author_id IN (select DISTINCT ba.author_id from book2author ba where ba.book_id IN (:bookIds))\n" +
            "),\n" +
            "\n" +
            "unique_book_ids_by_genre_ids as (\n" +
            "\tselect distinct bg.book_id\n" +
            "\tfrom book2genre bg\n" +
            "\twhere bg.book_id not in (:bookIds) \n" +
            "\t\tAND bg.genre_id IN (select DISTINCT bg.genre_id from book2genre bg where bg.book_id IN (:bookIds))\n" +
            "),\n" +
            "\n" +
            "union_queries as (\n" +
            "\tselect * from unique_book_ids_by_tag_ids\n" +
            "\tunion\n" +
            "\tselect * from unique_book_ids_by_author_ids\n" +
            "\tunion\n" +
            "\tselect * from unique_book_ids_by_genre_ids\n" +
            ")\n" +
            "\n" +
            "select \tbs.* \n" +
            "from books bs\n" +
            "where bs.id IN (select * from union_queries)\n" +
            "order by pub_date desc", nativeQuery = true)
    List<Book> getRecommendedBooksForAuthorizedUserWithCookieStrings(@Param("bookIds") List<Integer> bookIds,
                                                                     Pageable nextPage);
}
