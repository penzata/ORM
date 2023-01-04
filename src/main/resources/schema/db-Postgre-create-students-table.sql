CREATE TABLE students
(
    id SERIAL PRIMARY KEY NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    enterSchoolDate DATE NOT NULL
)