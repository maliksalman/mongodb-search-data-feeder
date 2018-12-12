## ElasticSearch Feeder

Feeds an elastic-search index with some randomly generated data

### ElasticSearch in docker

For testing purposes, start a single node elastic-search cluster in docker:

```
docker run -p 9200:9200 -p 9300:9300 -d --rm --name elasticsearch -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.4.2

```

to stop the container

```
docker stop elasticsearch
```

### Adding data to the cluster

The below commands will add 100,000 documents to the "myindex" with a batch-size of 5000

```
./gradlew clean build
java -jar build/libs/elastic-search-0.0.1-SNAPSHOT.jar localhost:9200 100000 myindex 5000
```