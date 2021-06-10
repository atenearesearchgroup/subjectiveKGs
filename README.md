
# subjectiveKGs
This repository contains additional material for the paper entitled EDOC21-SubjectiveKnowledgeGraphs  introducesSubjective Knowledge Graphs(SKG), an extension to ProbabilisticKnowledge  Graphs  that  considers  the  individual  opinions  of separate users about the same facts, and allows reasoning about them.Tis paer is currently under revision.

The repository includes code and datasets for reproducibility purposes, as well as extended experimental results.

## Requirements/dependencies

The code is provided as a Maven project, which has been developed in Netbans IDE 8.2 with Java 8.

The following dependecies are required:

* [Neo4j community edition version 4.2.2] (https://neo4j.com/download-center/#community), a  graph  database  management  system  with CRUD methods that expose a graph data model. This version uses a java implementation with Java 11.
* [Apoc Core plugin] (https://neo4j.com/labs/apoc/4.1/installation) The APOC library consists of many  procedures and functions to help with many different tasks in areas like data integration, graph algorithms or data conversion.

In in order to facilitate the configuration of neo4j I show you the essential configuration elements of ne4j configuration to be able to test all the experiments. The 

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

1. Clone the subjectiveKGs project on the machine where it has been installed in neo4j.
2. Build the project 

```
    cd $HOME_PROJECT
    JAVA_HOME=/... # if is not defined variable $JAVA_HOME the path should be specified
    mvn clean install  

```
3. Copy the file $HOME_PROJECT/target/neo4j_sBoolean-1.0-SNAPSHOT.jar to the $HOME_NEO4J/plugins directory specified in the neo4j.conf file.
4. Restart the neo4j service

## Loading SDK of motivating example
* First open a browser and connect to the neo4j client  (with this configuration, no credentials are required)
''' 
    http://neo4j_server_ip:7474/browser/
    
'''
* Copy the loading file [example1.graphml](https://github.com/atenearesearchgroup/subjectiveKGs/examples/example1.graphml) of nodes and relations from the motivator example into $HOME_NEO4J/import
* Run next statement of the copied graphml file in the neo4j client
'''
    CALL apoc.import.graphml("example1.graphml", {readLabels: true})

'''
* Finally copy the loading file of nodes and relations from the motivator example into $HOME_NEO4J/import

## NELL Datasets