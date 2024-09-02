# IDORIS

IDORIS is an **Integrated Data Type and Operations Registry with Inheritance System**.

## Installation of Neo4j

IDORIS relies on the Neo4j graph database.
The following command will start a Neo4j instance with the necessary plugins and configuration.
If you want demo data to be loaded, execute this command in this directory.
All data will be stored in the `neo4j-data` directory.
The password for the Neo4j user `neo4j` is `superSecret`.
You can access the Neo4j browser at http://localhost:7474.

```
docker run \
   -p 7474:7474 -p 7687:7687 \
   --name neo4j-apoc-gds-idoris \
   --volume=$(pwd)/neo4j-data:/data \
   -e NEO4J_AUTH=neo4j/superSecret \
   -e NEO4J_DEBUG=true \
   -e NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
   -e NEO4J_apoc_export_file_enabled=true \
   -e NEO4J_apoc_import_file_enabled=true \
   -e NEO4J_apoc_import_file_use__neo4j__config=true \
   -e NEO4J_PLUGINS=\[\"apoc\",\"graph-data-science\"\,\"bloom\"] \
   -e NEO4J_dbms_security_procedures_unrestricted=apoc.\\\*,gds.\\\* \
   neo4j:latest
```

## Running IDORIS

Configure the application.properties file to contain the Neo4j API credentials.

```
spring.application.name=idoris
logging.level.root=INFO
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=superSecret
spring.data.rest.basePath=/api
server.port=8095
idoris.validation-level=info
idoris.validation-policy=strict
```

When Neo4j is running, start IDORIS with the following command:

```
./gradlew bootRun
```