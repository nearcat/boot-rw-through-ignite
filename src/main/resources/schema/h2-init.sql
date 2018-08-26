DROP TABLE IF EXISTS books CASCADE;

CREATE TABLE books (
  isbn varchar(50) NOT NULL,
  title varchar(50) NOT NULL,
  PRIMARY KEY (title)
);

INSERT INTO books VALUES ('ISBN1', 'Spring boot With Apache Ignite 1');
INSERT INTO books VALUES ('ISBN2', 'Spring boot With Apache Ignite 2');
INSERT INTO books VALUES ('ISBN3', 'Spring boot With Apache Ignite 3');
INSERT INTO books VALUES ('ISBN4', 'Spring boot With Apache Ignite 4');
INSERT INTO books VALUES ('ISBN5', 'Spring boot With Apache Ignite 5');
INSERT INTO books VALUES ('ISBN6', 'Spring boot With Apache Ignite 6');
