package com.containersolutions.mesoshelloworld.scheduler;

import org.apache.mesos.Protos.*;

import java.util.UUID;


public class TaskInfoFactory {

    private final Configuration configuration;

    public TaskInfoFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    TaskInfo newTask(Offer offer, Scheduler.ResourceOffer currentOffer) {
        TaskID taskId = TaskID.newBuilder().setValue(UUID.randomUUID().toString()).build();

        System.out.println("Launching task " + taskId.getValue() +
                " using offer " + offer.getId().getValue());

        Long port = currentOffer.offerPorts.get(0);
        Value.Range singlePortRange = Value.Range.newBuilder().setBegin(port).setEnd(port).build();

        TaskInfo task = TaskInfo.newBuilder()
                .setName("task " + taskId.getValue())
                .setTaskId(taskId)
                .setSlaveId(offer.getSlaveId())
                .addResources(Resource.newBuilder()
                        .setName("cpus")
                        .setType(Value.Type.SCALAR)
                        .setScalar(Value.Scalar.newBuilder().setValue(Scheduler.CPUS_PER_TASK)))
                .addResources(Resource.newBuilder()
                        .setName("mem")
                        .setType(Value.Type.SCALAR)
                        .setScalar(Value.Scalar.newBuilder().setValue(Scheduler.MEM_PER_TASK)))
                .addResources(Resource.newBuilder()
                                .setName("ports")
                                .setType(Value.Type.RANGES)
                                .setRanges(Value.Ranges.newBuilder().addRange(singlePortRange))
                                .build()
                )
                .setExecutor(newExecutorInfo(configuration))
                .setDiscovery(newDiscoveryInfo(port.intValue()))
                .build();


        currentOffer.offerCpus -= Scheduler.CPUS_PER_TASK;
        currentOffer.offerMem -= Scheduler.MEM_PER_TASK;
        currentOffer.offerPorts.remove(0);
        return task;
    }

    DiscoveryInfo.Builder newDiscoveryInfo(Integer port) {
        DiscoveryInfo.Builder discovery = DiscoveryInfo.newBuilder();
        Ports.Builder discoveryPorts = Ports.newBuilder();
        discoveryPorts.addPorts(0, Port.newBuilder().setNumber(port).setName("port"));
        discovery.setPorts(discoveryPorts);
        discovery.setVisibility(DiscoveryInfo.Visibility.EXTERNAL);
        return discovery;
    }

    ExecutorInfo.Builder newExecutorInfo(Configuration configuration) {
        return ExecutorInfo.newBuilder()
                .setExecutorId(ExecutorID.newBuilder().setValue(UUID.randomUUID().toString()))
                .setName("hello-world-executor-" + UUID.randomUUID().toString())
                .setCommand(newCommandInfo(configuration))
                .setContainer(ContainerInfo.newBuilder()
                        .setType(ContainerInfo.Type.DOCKER)
                        .setDocker(ContainerInfo.DockerInfo.newBuilder().setNetwork(ContainerInfo.DockerInfo.Network.HOST).setImage(configuration.getExecutorImage()).setForcePullImage(configuration.getExecutorForcePullImage()))
                        .build());
    }

    CommandInfo.Builder newCommandInfo(Configuration configuration) {
        return CommandInfo.newBuilder()
                .setShell(false)
                .setContainer(CommandInfo.ContainerInfo.newBuilder().setImage(configuration.getExecutorImage()).build());
    }


}