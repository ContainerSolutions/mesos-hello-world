package com.containersolutions.mesoshelloworld.systemtest;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jayway.awaitility.Awaitility.await;

/**
 * Response which waits until endpoint is ready
 */
public class HelloWorldResponse {
    public static final Logger LOGGER = Logger.getLogger(HelloWorldResponse.class);
    private boolean discoverySuccessful;

    public HelloWorldResponse(String ip, List<Integer> ports) {
        List<String> ipAddresses = ports.stream().map(p -> "http://" + ip + ":" + p).collect(Collectors.toList());
        await().atMost(60, TimeUnit.SECONDS).until(new TasksCall(ipAddresses));
    }

    public boolean isDiscoverySuccessful() {
        return discoverySuccessful;
    }

    class TasksCall implements Callable<Boolean> {
        private final List<String> urls;

        public TasksCall(List<String> urls) {
            this.urls = urls;
        }

        @Override
        public Boolean call() throws Exception {
            urls.forEach(url -> {
                try {
                    LOGGER.debug(Unirest.get(url).asString());
                    discoverySuccessful = true;
                } catch (UnirestException e) {
                    LOGGER.debug("Waiting until " + urls.size() + " webapps are started...");
                    discoverySuccessful = false;
                }
            });
            return discoverySuccessful;
        }
    }
}
