package com.containersolutions.mesoshelloworld.systemtest;

import org.apache.log4j.Logger;
import com.containersol.minimesos.MesosCluster;
import com.containersol.minimesos.mesos.MesosClusterConfig;
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
            .slaveResources(new String[]{"ports(*):[8080-8082]", "ports(*):[8080-8082]", "ports(*):[8080-8082]"})
            .build();

    @ClassRule
    public static final MesosCluster CLUSTER = new MesosCluster(CONFIG);

    @BeforeClass
    public static void startScheduler() throws Exception {

        LOGGER.info("Starting Scheduler");
        String ipAddress = CLUSTER.getMesosMasterContainer().getIpAddress();
        SchedulerContainer scheduler = new SchedulerContainer(CONFIG.dockerClient, ipAddress);

        // Cluster now has responsibility to shut down container
        CLUSTER.addAndStartContainer(scheduler);

        LOGGER.info("Started Elasticsearch scheduler on " + scheduler.getIpAddress());
    }

    @Test
    public void testNodeDiscoveryRest() {
        String ipAddress = CLUSTER.getMesosMasterContainer().getIpAddress();
        HelloWorldResponse helloWorldResponse = new HelloWorldResponse( ipAddress, Arrays.asList(8080, 8081, 8082));
        assertTrue("Elasticsearch nodes did not discover each other within 1 minute", helloWorldResponse.isDiscoverySuccessful());
    }

}
