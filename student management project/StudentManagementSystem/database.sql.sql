CREATE DATABASE student_management;

USE student_management;

CREATE TABLE students(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    course VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(15)
);

CREATE TABLE admin(
    username VARCHAR(50),
    password VARCHAR(50)
);

INSERT INTO admin VALUES('admin','admin123');