package com.containersolutions.mesoshelloworld.systemtest;

import org.apache.log4j.Logger;
import org.apache.mesos.mini.MesosCluster;
import org.apache.mesos.mini.mesos.MesosClusterConfig;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Tests REST node discovery
 */
public class DiscoverySystemTest {
    public static final Logger LOGGER = Logger.getLogger(DiscoverySystemTest.class);

    protected static final MesosClusterConfig CONFIG = MesosClusterConfig.builder()
            .numberOfSlaves(3)
            .privateRegistryPort(15000) // Currently you have to choose an available port by yourself
            .slaveResources(new String[]{"ports(*):[8080-8082]", "ports(*):[8080-8082]", "ports(*):[8080-8082]"})
            .build();

    @ClassRule
    public static final MesosCluster CLUSTER = new MesosCluster(CONFIG);

    private static SchedulerContainer scheduler;

    @BeforeClass
    public static void startScheduler() throws Exception {
        CLUSTER.injectImage("containersol/mesos-hello-world-executor");
        LOGGER.info("Starting Scheduler");
        scheduler = new SchedulerContainer(CONFIG.dockerClient, CLUSTER.getMesosContainer().getIpAddress());
        CLUSTER.addAndStartContainer(scheduler); // Cluster now has responsibility to shut down container.
        LOGGER.info("Started Elasticsearch scheduler on " + scheduler.getIpAddress());
    }

    @Test
    public void testNodeDiscoveryRest() {
        HelloWorldResponse helloWorldResponse = new HelloWorldResponse(CLUSTER.getMesosContainer().getIpAddress(), Arrays.asList(8080, 8081, 8082));
        assertTrue("Elasticsearch nodes did not discover each other within 1 minute", helloWorldResponse.isDiscoverySuccessful());
    }

}
