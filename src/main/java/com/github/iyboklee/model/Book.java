package com.github.iyboklee.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Book implements Serializable {

    private String isbn;

    private String title;

}
