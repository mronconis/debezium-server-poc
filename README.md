# PoC debezium-server

## Setup

````bash
export DEBEZIUM_VERSION=2.4

# Initialize MongoDB
docker compose up -d mongodb

# Initialize MongoDB replica set and insert some test data
docker compose exec mongodb bash -c '/usr/local/bin/init-inventory.sh'

# Initialize Kafka broker
docker compose up -d kafka

# Initialize Debezium Server
docker compose up -d debezium-server
````

## Dump offsets

````bash
# Build offset-formatter
cd offset-formatter && mvn clean package && cd -

# Copy offset.dat file from debezium-server container
docker cp debezium-server:/tmp/offsets.dat /tmp/offsets.dat

# Read offset.dat file
java -jar offset-formatter/target/offset-formatter-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp/offsets.dat
````

Output key:
````json
[
    "kafka",
    {
        "rs": "rs0",
        "server_id": "tutorial"
    }
]
````
Output value:
````json
{
    "sec": 1690985642,
    "ord": 1,
    "transaction_id": null,
    "resume_token": "8264CA64AA000000012B022C0100296E5A1004EC8904C0A5C64C609F8BC7E3FA983506461E5F6964002C07DC0004"
}
````

## Verify
````bash
# List topics
docker exec -it kafka bin/kafka-topics.sh \
    --bootstrap-server kafka:9092 \
    --list

# Consume topic
docker exec -it kafka bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --topic tutorial.inventory.customers \
    --from-beginning
````