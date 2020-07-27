# External Services for Integration Test

This folder contains docker files to run external services for itests such  http server.


## Start Services

   $ docker-compose up
   $ docker-compose down

docker compose creates and run following containers:

- scoopi-dev-nginx  nginx http server

## Nginx Http Server

src/main/itservices/nginx contains nginx related files.

nginx/www - contains test html files
nginx/conf.d - contains default conf to handle page redirect to test 301
