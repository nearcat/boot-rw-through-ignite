DROP TABLE IF EXISTS books CASCADE;

CREATE TABLE books (
  isbn varchar(50) NOT NULL,
  title varchar(50) NOT NULL,
  PRIMARY KEY (title)
);

INSERT INTO books VALUES ('ISBN1', 'Spring Boot With Ignite1');
INSERT INTO books VALUES ('ISBN2', 'Spring Boot With Ignite2');
INSERT INTO books VALUES ('ISBN3', 'Spring Boot With Ignite3');
INSERT INTO books VALUES ('ISBN4', 'Spring Boot With Ignite4');
INSERT INTO books VALUES ('ISBN5', 'Spring Boot With Ignite5');
INSERT INTO books VALUES ('ISBN6', 'Spring Boot With Ignite6');
