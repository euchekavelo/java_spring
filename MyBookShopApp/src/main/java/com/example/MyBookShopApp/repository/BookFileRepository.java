package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.BookFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@DebugLogs
public interface BookFileRepository extends JpaRepository<BookFile, Integer> {

    BookFile findBookFileByHash(String hash);
}
