package com.containersolutions.mesoshelloworld.scheduler;

import org.apache.mesos.Protos.*;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapted from: https://github.com/apache/mesos/blob/0.22.1/src/examples/java/TestFramework.java
 */
public class Scheduler implements org.apache.mesos.Scheduler {

    public static final double CPUS_PER_TASK = 0.1;
    public static final double MEM_PER_TASK = 128;
    private final Configuration configuration;
    private int launchedTasks = 0;

    public Scheduler(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void resourceOffers(SchedulerDriver driver,
                               List<Offer> offers) {
        List<TaskInfo> newTaskList = new ArrayList<>();
        for (Offer offer : offers) {
            ResourceOffer currentOffer = new ResourceOffer(offer.getResourcesList());

            System.out.println(
                    "Received offer " + offer.getId().getValue() + " with cpus: " + currentOffer.offerCpus +
                            " and mem: " + currentOffer.offerMem + " with ports: " + currentOffer.offerPorts);

            while (launchedTasks < configuration.getExecutorNumber() &&
                    currentOffer.offerCpus >= CPUS_PER_TASK &&
                    currentOffer.offerMem >= MEM_PER_TASK &&
                    currentOffer.offerPorts.size() >= 1) {
                TaskInfo task = new TaskInfoFactory(configuration).newTask(offer, currentOffer);
                newTaskList.add(task);
            }
            driver.launchTasks(offer.getId(), newTaskList);
        }
    }

    @Override
    public void offerRescinded(SchedulerDriver driver, OfferID offerId) {
    }

    @Override
    public void statusUpdate(SchedulerDriver driver, TaskStatus status) {
        System.out.println("Status update: task " + status.getTaskId().getValue() +
                " is in state " + status.getState().getValueDescriptor().getName());
        // Intentionally don't handle state. Just a demo.
    }

    @Override
    public void frameworkMessage(SchedulerDriver driver,
                                 ExecutorID executorId,
                                 SlaveID slaveId,
                                 byte[] data) {
    }

    @Override
    public void slaveLost(SchedulerDriver driver, SlaveID slaveId) {
    }

    @Override
    public void executorLost(SchedulerDriver driver,
                             ExecutorID executorId,
                             SlaveID slaveId,
                             int status) {
    }

    @Override
    public void registered(SchedulerDriver driver,
                           FrameworkID frameworkId,
                           MasterInfo masterInfo) {
        System.out.println("Registered! ID = " + frameworkId.getValue());
    }

    @Override
    public void reregistered(SchedulerDriver driver, MasterInfo masterInfo) {
    }

    @Override
    public void disconnected(SchedulerDriver driver) {
    }

    @Override
    public void error(SchedulerDriver driver, String message) {
        System.out.println("Error: " + message);
    }

    class ResourceOffer {

        final List<Long> offerPorts;
        double offerCpus = 0;
        double offerMem = 0;

        public ResourceOffer(List<Resource> resourcesList) {
            offerPorts = new ArrayList<>(resourcesList.size());
            for (Resource resource : resourcesList) {
                if (resource.getName().equals("cpus")) {
                    offerCpus += resource.getScalar().getValue();
                } else if (resource.getName().equals("mem")) {
                    offerMem += resource.getScalar().getValue();
                } else if (resource.getName().equals("ports")) {
                    for (Long p = resource.getRanges().getRange(0).getBegin(); p <= resource.getRanges().getRange(0).getEnd(); p++) {
                        offerPorts.add(p);
                    }
                }
            }
        }
    }
}