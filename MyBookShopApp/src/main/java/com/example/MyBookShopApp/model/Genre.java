package com.example.MyBookShopApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;
@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Genre parent;

    private String slug;

    private String name;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Genre> childList;

    @ManyToMany(mappedBy = "genreList")
    @JsonIgnore
    private List<Book> bookList;

    public Genre getParent() {
        return parent;
    }

    public List<Genre> getChildList() {
        return childList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
