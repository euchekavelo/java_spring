package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.BookFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookFileRepository extends JpaRepository<BookFile, Integer> {

    BookFile findBookFileByHash(String hash);
}
