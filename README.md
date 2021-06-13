
# subjectiveKGs
This repository contains additional material for the paper entitled **Introducing Subjective Knowledge Graphs** initiates  an extension to Probabilistic Knowledge Graphs that considers the  individual  opinions  of separate users about the same facts, and allows reasoning about them. This paper is currently under revision.

The repository includes code and datasets for reproducibility purposes.

## Requirements/dependencies

The code is provided as a Maven project, which has been developed in Netbans IDE 8.2 with Java 8.

The following dependecies are required:

* [Neo4j community edition version 4.2.2](https://neo4j.com/download-center/#community), a  graph database  management  system  with CRUD methods that expose a graph data model. This version uses a java implementation with Java 11.
* [Apoc Core plugin](https://neo4j.com/labs/apoc/4.1/installation) The APOC library consists of many  procedures and functions to help with many different tasks in areas like data integration, graph algorithms or data conversion.

In in order to facilitate the configuration of Neo4j I show you the essential configuration elements of ne4j configuration to be able to test all the experiments. The 

At least the following lines in the neo4j.conf file (in the case of linux from the Archlinux distribution /etc/neo4j/neo4j.conf) in the $NEO4J_HOME directory  must be enabled.

```
    # Paths of directories in the installation.
    dbms.directories.data=/var/lib/neo4j/data
    dbms.directories.plugins=/usr/share/java/neo4j/plugins
    dbms.directories.certificates=/etc/neo4j/certificates
    dbms.directories.logs=/var/log/neo4j
    dbms.directories.lib=/usr/share/java/neo4j/lib
    dbms.directories.run=/run/neo4j

    #This setting constrains all `LOAD CSV` import files to be under the `import` directory. Remove or comment it out to
    # allow files to be loaded from anywhere in the filesystem; this introduces possible security problems. See the
    # `LOAD CSV` section of the manual for details.
    dbms.directories.import=/var/lib/neo4j/import

    # Whether requests to Neo4j are authenticated.
    # To disable authentication, uncomment this line
    dbms.security.auth_enabled=false

    # Java Heap Size: by default the Java heap size is dynamically
    # calculated based on available system resources.
    # Uncomment these lines to set specific initial and maximum
    # heap size.
    dbms.memory.heap.initial_size=1G
    dbms.memory.heap.max_size=2G

    # The amount of memory to use for mapping the store files, in bytes (or
    # kilobytes with the 'k' suffix, megabytes with 'm' and gigabytes with 'g').
    # If Neo4j is running on a dedicated server, then it is generally recommended
    # to leave about 2-4 gigabytes for the operating system, give the JVM enough
    # heap to hold all your transaction state and query context, and then leave the
    # rest for the page cache.
    # The default page cache memory assumes the machine is dedicated to running
    # Neo4j, and is heuristically set to 50% of RAM minus the max Java heap size.
    dbms.memory.pagecache.size=3g

    #********************************************************************
    # Other Neo4j system properties
    #********************************************************************
    dbms.jvm.additional=-Dunsupported.dbms.udc.source=tarball
    dbms.unmanaged_extension_classes=n10s.endpoint=/rdf
    dbms.security.procedures.unrestricted=apoc.*
    dbms.logs.query.enabled=true
```
## Install sboolean.util plugin
To install the plugin with the SBooleans handling procedures and functions in Neo4j we need to

1. Clone the subjectiveKGs project on the machine where it has been installed in Neo4j.
2. Build the project 

```
    cd $HOME_PROJECT
    JAVA_HOME=/... # if is not defined variable $JAVA_HOME the path should be specified
    mvn clean install  
```
3. Copy the file $HOME_PROJECT/target/neo4j_sBoolean-1.0-SNAPSHOT.jar to the $HOME_NEO4J/plugins directory specified in the neo4j.conf file.
4. Restart the neo4j service

## Loading SKG of motivating example
* First open a browser and connect to the Neo4j client  (with this configuration, no credentials are required)

```
    http://neo4j_server_ip:7474/browser/
```  

* Copy the loading file [example1.graphml](https://github.com/atenearesearchgroup/subjectiveKGs/examples/example1.graphml) of nodes and relations from the motivator example into $HOME_NEO4J/import
* Run next statement of the copied graphml file in the Neo4j client

```
    CALL apoc.import.graphml("example1.graphml", {readLabels: true})
```

* Finally copy the loading file of nodes and relations from the motivator example into $HOME_NEO4J/import

## Loading SKG of NELL bastketbal players example
* First open a browser and connect to the Neo4j client  (with this configuration, no credentials are required)

``` 
    http://neo4j_server_ip:7474/browser/ 
```

* Copy the loading file [example2.graphml](https://github.com/atenearesearchgroup/subjectiveKGs/examples/example2.graphml) of nodes and relations from the NELL basketbal players example into $HOME_NEO4J/import
* Run next statement of the copied graphml file in the Neo4j client

```
    CALL apoc.import.graphml("example2.graphml", {readLabels: true})
```

## Loading SKG of NELL hotels to follow the league example
* First open a browser and connect to the Neo4j client  (with this configuration, no credentials are required)

``` 
    http://neo4j_server_ip:7474/browser/ 
```

* Copy the loading file [example4.graphml](https://github.com/atenearesearchgroup/subjectiveKGs/examples/example4.graphml) of nodes and relations from the NELL basketbal players example into $HOME_NEO4J/import
* Run next statement of the copied graphml file in the Neo4j client

```
    CALL apoc.import.graphml("example4.graphml", {readLabels: true})
```

## Loading SKG of NELL Cities  and  their  relations example
* First open a browser and connect to the Neo4j client  (with this configuration, no credentials are required)

``` 
    http://neo4j_server_ip:7474/browser/ 
```

* Copy the loading file [example4.graphml](https://github.com/atenearesearchgroup/subjectiveKGs/examples/example4.graphml) of nodes and relations from the NELL basketbal players example into $HOME_NEO4J/import
* Run next statement of the copied graphml file in the Neo4j client

```
    CALL apoc.import.graphml("example4.graphml", {readLabels: true})
```

## PKG based on NELL repository
In this section we intend to illustrate our approach with a real probabilistic knowledge graph basis. For this purpose we have relied on the construction of the knowledge graph from the category and relationship data of the NELL repository.

### NELL Project Overview
NELL [(Never-Ending Language Learner)](http://rtw.ml.cmu.edu/rtw/) is a knowledge base of structured information that mirrors the content of the Web. The main objective of the NELL project is to build a never-ending machine learning system that acquires the ability to extract structured information from unstructured web pages. The inputs to NELL include  an initial ontology defining hundreds of categories (e.g., person, sportsTeam, fruit, emotion) and relations (e.g., playsOnTeam(athlete,sportsTeam), playsInstrument(musician,instrument)) that NELL is expected to extract, and 10 to 15 seed examples of each category and relation.

NELL explores information in addition to a collection of 500 million web pages through search engine APIs, to perform two ongoing tasks:

* Extract new instances of categories and relations. In other words, find noun phrases that represent new examples of the input categories that are added to the growing knowledge base of structured beliefs.
* Learning how to extract better data and insights on a daily basis using a variety of methods to extract beliefs from the web. These are retrained, using the growing knowledge base as a self-supervised collection of training examples. The result is a semi-supervised learning method that couples the training of hundreds of different extraction methods for a wide range of categories and relations


### Loading data from NELL to Neo4j
Data triples have been downloaded from the NELL resources and data section. Each line of this file contains one category or relation instance that NELL believes to be true. Nominally, each belief is an (Entity, Relation, Value) triple. The selected file columns are as follows:

* Entity: The Entity part of the (Entity, Relation, Value) triple.
* Relation: The Relation part of the (Entity, Relation, Value) triple. In the case of a category instance, this will be "generalizations". In the case of a relation instance, this will be the name of the relation.
* Value: The Value part of the (Entity, Relation, Value) triple. In the case of a category instance, this will be the name of the category. In the case of a relation instance, this will be another concept (like Entity).
* Probability: A confidence score for the belief. Note that NELL's scores are not actually probabilistic at this time.
* Entity literalStrings: The set of actual textual strings that NELL has read that it believes can refer to the concept indicated in the Entity column.


Using specific Neo4j commands, NELL nodes and relations have been loaded, obtaining a set of 2,121,179 nodes and 644,899 relations that have been labeled and typed by means of data refinement operations in Neo4j.

The files containing the simplified Nell triples for both nodes and relations can be downloaded from the repository and are named [ent.csv](https://github.com/atenearesearchgroup/subjectiveKGs/examples/ent.csv.tar.gz) and [rel.csv](https://github.com/atenearesearchgroup/subjectiveKGs/examples/rel.csv.tar.gz). They should be located in $HOME_NEO4J/import.

Due to space limitations in the repository, the file ent.csv had to be split into ent_01.csv and ent_02.csv.

This te command to load the entity nodes and their clases.

```
:auto USING PERIODIC COMMIT 100 LOAD CSV FROM 'file:///ent.awk.csv' AS row FIELDTERMINATOR '|'
MERGE (e: Entity {entityId: row[0]})
WITH e, row
CALL apoc.create.setLabels(e, apoc.coll.toSet(split(row[0],':') + apoc.node.labels(e))) yield node
MERGE (g: Generalization {entityId: row[2]})
MERGE (g)-[r:IsGeneralization {probability: coalesce(toFloat(row[4]),toFloat(0))}]->(node)
WITH node as n, row, g
CALL apoc.create.setLabels(g, apoc.coll.toSet(split(row[2],':') + apoc.node.labels(g))) yield node
WITH n, row, node as g
CALL apoc.create.setProperties(n, ['bestLiteralString', 'entityId'], [row[5], row[0]]) yield node
UNWIND split(row[4], '$') AS literalString
MERGE (l:LiteralString {name: literalString})
MERGE (node)-[r:IsNamedLiteral]->(l)
```

This te command to load the relations between nodes.

```
:auto USING PERIODIC COMMIT 1000 LOAD CSV FROM 'file:///hotel.ent.awk.csv' AS row FIELDTERMINATOR '|'
WITH row
CALL apoc.merge.node(['Entity'] + apoc.coll.toSet(split(row[0],':')) , {entityId: row[0], bestLiteralString: row[5]}) yield node
MERGE (g: Category {entityId: row[2]})
MERGE (g)-[r:IsGeneralization {probability: coalesce(toFloat(row[4]),toFloat(0))}]->(node)
WITH node, row
UNWIND split(row[4], '$') AS literalString
MERGE (l:LiteralString {name: literalString})
MERGE (node)-[r:IsNamedLiteral]->(l)
```


