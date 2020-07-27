package org.codetab.scoopi.itest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.Uninterruptibles;

public class ITestClusterBase extends ITestBase {

    @Override
    protected Properties getCommonProperties(final String defDir) {
        Properties properties = super.getCommonProperties(defDir);
        properties.put("scoopi.cluster.enable", "true");
        properties.put("scoopi.cluster.quorum.size", "2");
        return properties;
    }

    protected void runScoopiCluster(final Map<String, Integer> nodesMap,
            final Properties properties) throws IOException {

        Map<String, Process> nodes = new HashMap<>();

        for (String nodeName : nodesMap.keySet()) {
            properties.put("scoopi.log.dir", "logs/" + nodeName);
            nodes.put(nodeName, runScoopiProcess(properties));
        }

        Map<String, CompletableFuture<Boolean>> awaits = new HashMap<>();
        for (String nodeName : nodes.keySet()) {
            Process node = nodes.get(nodeName);
            awaits.put(nodeName, awaitCompletion(node));
        }

        LinkedHashMap<String, Integer> sortedTimeoutMap = new LinkedHashMap<>();

        nodesMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .forEachOrdered(
                        x -> sortedTimeoutMap.put(x.getKey(), x.getValue()));

        for (String nodeName : sortedTimeoutMap.keySet()) {
            CompletableFuture<Boolean> await = awaits.get(nodeName);
            int timeout = sortedTimeoutMap.get(nodeName);
            Process node = nodes.get(nodeName);

            try {
                await.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException
                    | TimeoutException e) {
                await.cancel(true);
                node.destroy();
            }
        }
        System.out.println("");
    }

    protected Process runScoopiProcess(final Properties properties)
            throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-Djava.class.path=" + System.getProperty("java.class.path"));
        for (Object key : properties.keySet()) {
            cmd.add(String.format("-D%s=%s", key, properties.get(key)));
        }
        cmd.add("org.codetab.scoopi.Scoopi");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        return pb.start();
    }

    protected CompletableFuture<Boolean> awaitCompletion(
            final Process process) {
        return CompletableFuture.supplyAsync(() -> {
            int c = 0;
            final int d = 3;
            while (process.isAlive()) {
                if ((++c) % d == 0) {
                    System.out.print(".");
                }
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
            return true;
        });
    }
}
