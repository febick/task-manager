# Task-manager
_The home task for Intuit_

## Exampe of configuration to run the application
### _docker-compose.yml_
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
For running:
```
docker-compose up -d
```

# API Requests
Specifying paths for requests and passed parameters.

## Adding new task
* _POST: **/tasks**_

The request must contain a body:
```
{
    "task": "The Naive task",
    "type": "naive",
    "priority": "low"
}
```
The **_type_** parameter can have one of the following values: 
* naive
* fifo
* priority

The **_priority_** parameter can have one of the following values: 
* low
* medium
* high

The response in case of successful addition should have the following structure:
```
{
    "pid": 1,
    "task": "The Naive task",
    "priority": "LOW",
    "created": "2022-05-03T05:33:23.185839086"
}
```
## Getting tasks
* _GET: **/tasks/{id}**_ - to get a task by ID
* _GET: **/tasks/**_ - to get all task sorted by default (date)
* _GET: **/tasks/sortedBy/{sort-type}**_ - to get all task sorted by specified parameter

The **_sort-type_** parameter can have one of the following values: 
* date
* id
* priority

## Deleting tasks
* _DELETE: **/tasks/remove/{id}**_ - to remove a task by ID
* _DELETE: **/tasks/remove/all**_ - to remove all tasks
* _DELETE: **/tasks/remove/all/{priority-type}**_ - to remove all tasks with specified priority

The **_priority-type_** parameter can have one of the following values: 
* low
* medium
* high

To delete a list of tasks, the request must have a body:
* _DELETE: **/tasks/remove/**_ 
```
{
    "list": [{id}, {id}, {id}]
}
```
If, when deleting a task or task list, at least one ID is found to be non-existent, an error will be returned:
```
{
    "error": "Process with id {id} wasn't found",
    "timestamp": "2022-05-03T06:01:41.079561136"
}
```
If successful, all delete operations return a list of the removed elements:
```
[
    {
        "pid": 8,
        "task": "The Naive task",
        "priority": "HIGH",
        "created": "2022-05-03T06:03:38.116468"
    },
    {
        "pid": 9,
        "task": "The FIFO task",
        "priority": "LOW",
        "created": "2022-05-03T06:03:40.205425"
    },
    {
        "pid": 10,
        "task": "The Priority task",
        "priority": "MEDIUM",
        "created": "2022-05-03T06:03:41.875962"
    }
]
```
