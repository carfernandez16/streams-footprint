# Footprint Registration During Massive Data Processing
Este repositorio contiene los recursos de la charla "Registro de Huella Durante el Procesamiento Masivo de Datos"

## Build Prerequisites

1. gradle 6.7
2. jdk 11

## Deployment Instructions

- Start containers using docker-compose
```
docker-compose up -d kafka elasticsearch
```

- Create topics

```
docker-compose exec kafka sh -c "kafka-topics --create --topic ___topic___1 --zookeeper zookeeper:2181 --partitions 1 --replication-factor 1"
```
```
docker-compose exec kafka sh -c "kafka-topics --create --topic ___topic___2 --zookeeper zookeeper:2181 --partitions 1 --replication-factor 1"
```
```
docker-compose exec kafka sh -c "kafka-topics --create --topic ___topic___3 --zookeeper zookeeper:2181 --partitions 1 --replication-factor 1"
```

- Build Apps

```
./gradlew app1:clean app1:assemble
```
```
./gradlew app2:clean app2:assemble
```
```
./gradlew footprinter:clean footprinter:assemble
```

- Run Apps
```
./gradlew app1:run
```
```
./gradlew app2:run
```
```
./gradlew footprinter:run
```

## Test
```
{
  "code":"678923AB",
  "time":1600291504,
  "temperature":10.5,
  "humidity":20
}
```

- Start a producer
```
docker-compose exec kafka sh -c "kafka-console-producer --topic ___topic___1 --broker-list kafka:9092 --property parse.key=true --property key.separator=,"
```
- Publish a raw message <key,value>
```
sensor_read_001,{"code":"678923AB","time":1600291504,"temperature":10.5,"humidity":20}
``` 

## How to get data from Elasticsearch

- Get indexes
```
curl --location --request GET 'http://localhost:9200/_cat/indices?v'
```
- Get Documents
```
curl --request GET --url 'http://localhost:9200/jmicros-streams/_search?scroll=10m&size=50' --header 'cache-control: no-cache' --header 'postman-token: d7dbffe1-f9b8-13a8-2116-02e3b9ca2228'
```
- Get Route
```
curl --request POST \
  --url http://localhost:9200/jmicros-streams/_search \
  --header 'cache-control: no-cache' \
  --header 'content-type: application/json' \
  --header 'postman-token: b5e624f4-754a-5a89-dba7-6189c9bc5f81' \
  --data '{"query":{"match":{"message.code":"678923AB"}},"_source":["topic"]}'
```
- Delete Index
```
curl --request DELETE --url http://localhost:9200/jmicros-streams --header 'cache-control: no-cache' --header 'postman-token: 4a6c0821-802b-547b-26f3-d15187e15413'
```