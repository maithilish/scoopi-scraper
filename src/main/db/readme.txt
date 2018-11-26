database for 

 -- scoopidev - development 
 -- scoopitest - integration tests
 -- scoopi - non production live

start db 
 
   $ docker-compose up
    
docker creates scoopidev database and schema.sql creates scoopi and scoopitest databases and user and prevs. 
In case not created then connect with mysql client and create db and user
   
   $ mysql -u root -proot -h 127.0.0.1 -P3306

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
	
stop db
 
   $ docker-compose down

