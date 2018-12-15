## MongoDB Feeder

Feeds a MongoDb collection with some randomly generated data - intended for search

### MongoDB in docker

For testing purposes, start a single node elastic-search cluster in docker:

```
docker run --name mongo --rm -d -p 27017:27017 mongo:3.2 --smallfiles

```

to stop the container

```
docker stop mongo
```

### Adding data to the cluster

The below commands will add 100,000 documents to the "myindex" with a batch-size of 5000

```
./gradlew clean build
java -jar build/libs/mongodb-search-data-feeder-0.0.1-SNAPSHOT.jar localhost 100000 myindex 5000
```
