# External Services for Integration Test

This folder contains docker related files to run external services for itests such as db and http server.


## Start Services

   $ docker-compose up
   $ docker-compose down

docker compose creates and run following containers:

- scoopi-dev-nginx  nginx http server
- scoopi-dev-db     mysql database

## Nginx Http Server

src/main/itservices/nginx contains nginx related files.

nginx/www - contains test html files
nginx/conf.d - contains default conf to handle page redirect to test 301

## Databases

scoopi-dev-db container holds following databases: 

 -- scoopidev - development 
 -- scoopitest - integration tests
 -- scoopi - non production live

docker creates scoopi-dev-db container and scoopidev database; schema.sql creates scoopi and scoopitest databases and user and prevs.  In case schema.sql not executed then connect with mysql client and create db and user.
   
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
	
