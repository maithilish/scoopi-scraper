CREATE DATABASE scoopitest;
CREATE DATABASE scoopi;

CREATE USER 'foo'@'localhost' IDENTIFIED BY 'bar';
GRANT ALL PRIVILEGES ON scoopidev.* TO 'foo'@'localhost';
GRANT ALL PRIVILEGES ON scoopitest.* TO 'foo'@'localhost';
GRANT ALL PRIVILEGES ON scoopi.* TO 'foo'@'localhost';

CREATE USER 'foo'@'%' IDENTIFIED BY 'bar';
GRANT ALL PRIVILEGES ON scoopidev.* TO 'foo'@'%';
GRANT ALL PRIVILEGES ON scoopitest.* TO 'foo'@'%';
GRANT ALL PRIVILEGES ON scoopi.* TO 'foo'@'%';

