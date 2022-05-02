# task-manager
The home task for Intuit

# Sample configuration docker-compose.yml to run the application

```
services:
  db-server:
    container_name: db-server
    image: postgres
    ports: 
      - "5432:5432"
    environment:
      - POSTGRES_PASSWORD=12345.com
      - POSTGRES_DB=tasks

  app:
    container_name: app
    image:
      febick/arkadiy.broun
    ports:
      - "8080:8080"
    environment:
      - POSTGRES_PASSWORD=12345.com
      - POSTGRES_USERNAME=postgres
      - POSTGRES_DATABASE=tasks
      - SPRING_PROFILES_ACTIVE=docker
```
