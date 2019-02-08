BlockChain
==========

## Build

To get the project running see [Build Notes](BUILD-NOTES.MD).


## Execute

Experiments were executed via `docker stack` (`docker swarm init`) and by using a rest client. The `docker-compose.yaml`s containing configurations can be found in folder `experiments`and can be run via

```
docker stack deploy --compose-file experiment\<experiment-file> blockchain
```

## Documentation

Experiment results are contained in the folder `documentation` within the PowerPoint presentation _PPR;;Projekt;;Results.pptx_. The midterm presentation _PPR;;Projekt;;Blockchain.pptx_ is also in that folder.
