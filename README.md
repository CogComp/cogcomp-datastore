# CogComp-Datastore
[![Build Status](https://semaphoreci.com/api/v1/cogcomp/cogcomp-datastore/branches/master/badge.svg)](https://semaphoreci.com/cogcomp/cogcomp-datastore)
[![Build Status](http://morgoth.cs.illinois.edu:8080/buildStatus/icon?job=datastore)](http://morgoth.cs.illinois.edu:8080/job/datastore)

A thin wrapper over Minio that stores and retrieves immutable data on our machines and supports versioning.

It follows the Nexus model of publishing software, but for data. 

The datastore stores items, which can be files or directories. Every item belongs to a group, and has a name and a version. 
The group is just a string, but per convention it should be of the form org.allenai.corpora. The name is also just a string, but it should be CamelCase, for example WebSentences. The version is just an integer.

When you request an item from the datastore, it will download the item from our server and put it into the cache, which
is a file or directory on the local file system. The path it returns is a path to that file or directory.
If it's already there, it skips the download and simply returns the path. The default cache folder is 
`.cogcomp-datastore` under your home folder. 

Datastores have names. Currently, we have the public datastore, and the private one. public is world-accessible,
while private is limited to private users. The default datastore is private.

## Getting started

First create a datastore object: 

```java 
Datastore ds = new Datastore();
```

This will read the authentication information from `datastore-config.properties`. (see [this sample](src/main/resources/datastore-config-sample.properties) file)
Alternatively you can pass these config details as a constructor parameters. 

The *anonymous connection* is when only the end-point is clear (secret key and access key are unknown).
With this connection only the public files/folders could be accessed. 
Just like the previous case, specifying only the end-point point in the constructor would result in anonymous connection.  

```java
Datastore ds = new Datastore("url:port"); // where 'url' and 'port' are replaced your server's information
```


### Reading from the datastore

To get a file from the default datastore, simply call this:
```java 
// Get version 4 of GreedyParserModel.json in the group-id edu.cogcomp
File f = ds.getFile("edu.cogcomp", "GreedyParserModel.json", 4.0);
```

The files in public buckes can be accessed with anonymous connection. 

**Note:** There is no way to automatically get the latest version from the datastore. 
This is by design. If you depend on the "latest" version of an item, your results are not reproducible, because 
someone might publish a new version and thus change what your code does.

### Publishing to the datastore

There are two main ways to write to the datastore, one for files, and one for directories:
```java
// publish BigModel.json under the name "GreedyParserModel.json", version 4
ds.publishFile("edu.cogcomp", "GreedyParserModel.json", 4.0, "BigModel.json");
```
 
## Suggestions / bugs / comments? 
 
Use issues or pull requests. 
 
