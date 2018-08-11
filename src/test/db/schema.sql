CREATE USER 'foo'@'localhost' IDENTIFIED BY 'bar';
GRANT ALL PRIVILEGES ON gotztest.* TO 'foo'@'localhost';
CREATE USER 'foo'@'%' IDENTIFIED BY 'bar';
GRANT ALL PRIVILEGES ON scoopitest.* TO 'foo'@'%';
