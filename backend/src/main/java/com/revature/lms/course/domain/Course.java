package com.revature.lms.course.domain;

public class Course {

    private long course_id;
    private long trainer_id;
    private String title;
    private String category;
    private CourseStatus status;

    public Course(long course_id, long trainer_id, String title, String category, CourseStatus status) {
        this.course_id = course_id;
        this.trainer_id = trainer_id;
        this.title = title;
        this.category = category;
        this.status = status;
    }
//----------------------------------------------------------------------------------------------------------------------
    public long getCourse_id() {
        return course_id;
    }

    public long getTrainer_id() {
        return trainer_id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public CourseStatus getStatus() {
        return status;
    }
    //----------------------------------------------------------------------------------------------------------------------


    public void setCourse_id(long course_id) {
        this.course_id = course_id;
    }

    public void setTrainer_id(long trainer_id) {
        this.trainer_id = trainer_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }
}
