package com.revature.lms.course.repository;


import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository {
    //implementation of findByInstructor etc.

    public static void findByInstructor(String instructor){

    }

}
