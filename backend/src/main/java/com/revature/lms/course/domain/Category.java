package com.revature.lms.course.domain;

import jakarta.persistence.Column;

public class Category {


    @Column(nullable=false, unique=true, length = 100)
    private String name;



}
