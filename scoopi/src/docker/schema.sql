CREATE USER 'foo'@'localhost' IDENTIFIED BY 'bar';
GRANT ALL PRIVILEGES ON scoopi.* TO 'foo'@'localhost';
CREATE USER 'foo'@'%' IDENTIFIED BY 'bar';
GRANT ALL PRIVILEGES ON scoopi.* TO 'foo'@'%';
