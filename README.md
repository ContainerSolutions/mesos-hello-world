# mesos-hello-world
Very simple hello world mesos framework to demonstrate http://github.com/containersolutions/mini-mesos.

## NOTE - This project has been integrated into minimesos

Checkout the [test-framework module in minimesos](https://github.com/ContainerSolutions/minimesos/tree/master/test-framework-docker)

## Introduction
This project creates a very simple Apache Mesos framework, with dockerized scheduler and exector containers.
The goal is to use the scheduler to start a number of dummy "webapps" (webservers that show "Hello world" on /) 
then test the framework using the mini-mesos project.

## Non-linux users
If you are developing on an envrionment other than Linux, then you will need to use a VM. We use docker-machine.
You will then need to export the docker settings. E.g.:
```
docker-machine create -d virtualbox --virtualbox-memory 4096 --virtualbox-cpu-count 2 dev
eval "$(docker-machine env dev)"
```
To run the system tests, non-linux users will have to route all communications to the docker containers via the VM IP address:
```
sudo route -n delete 172.17.0.0/16 $(docker-machine ip dev) ; sudo route -n add 172.17.0.0/16 $(docker-machine ip dev)
```

## Compiling
To build the project (you will need to build the docker images before you run the system tests) simply run:
```
./gradlew build -x test
```

## System test
To run the system tests, run:
```
./gradlew :system-test:test
```
It is also possible to run the system test in Idea, by setting the JUnit environmental variables to point to your docker daemon.
