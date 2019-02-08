* We have to build a docker image for both bootnode and node.
* First we need to build the jar which is done via a gradle command.

```
gradlew :node:shadowJar
gradlew :node:shadowJar
```

* Then we build the docker images

```
docker build -t ppr/blockchain/node .\node\
docker build -t ppr/blockchain/bootnode .\bootnode\
```
