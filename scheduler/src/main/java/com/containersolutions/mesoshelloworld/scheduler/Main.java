package com.containersolutions.mesoshelloworld.scheduler;

import com.google.protobuf.ByteString;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;

/**
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration(args);

        Protos.FrameworkInfo.Builder frameworkBuilder = Protos.FrameworkInfo.newBuilder()
                .setPrincipal(configuration.getFrameworkPrincipal())
                .setUser("") // Have Mesos fill in the current user.
                .setName("Hello world example")
                .setCheckpoint(true);
        org.apache.mesos.Scheduler scheduler = new Scheduler(configuration);

        MesosSchedulerDriver driver =
            configuration.getFrameworkPrincipal() != null
              ? new MesosSchedulerDriver(
                        scheduler,
                        frameworkBuilder.build(),
                        configuration.getMesosMaster(),
                        Protos.Credential.newBuilder()
                                .setPrincipal(configuration.getFrameworkPrincipal())
                                .setSecret(ByteString.copyFromUtf8(configuration.getFrameworkSecret()))
                                .build()
                )
              : new MesosSchedulerDriver(
                    scheduler,
                    frameworkBuilder.build(),
                    configuration.getMesosMaster()
                );

        int status = driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;

        // Ensure that the driver process terminates.
        driver.stop();

        System.exit(status);
    }
}
