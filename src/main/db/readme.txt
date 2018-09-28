test database for integration tests
----

start db 
 
   $ docker-compose up
    
schema.sql creates user and prevs. In case not created 
then connect with mysql client and create db and user
   
   $ mysql -u root -proot -h 127.0.0.1 -P3301
	create database gotztest;
	CREATE USER 'foo'@'localhost' IDENTIFIED BY 'bar';
	GRANT ALL PRIVILEGES ON gotz.* TO 'foo'@'localhost';
	CREATE USER 'foo'@'%' IDENTIFIED BY 'bar';
	GRANT ALL PRIVILEGES ON gotz.* TO 'foo'@'%';
	
stop db
 
   $ docker-compose down

