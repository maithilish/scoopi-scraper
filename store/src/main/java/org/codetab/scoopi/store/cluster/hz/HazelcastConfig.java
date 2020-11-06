package org.codetab.scoopi.store.cluster.hz;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;

public class HazelcastConfig {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Get hazelcast server config.
     * @param fileName
     * @return
     */
    public Config getConfig(final String fileName) {
        Config cfg = new XmlConfigBuilder(
                Cluster.class.getResourceAsStream(fileName)).build();
        cfg.setProperty("hazelcast.shutdownhook.policy", "GRACEFUL");
        return cfg;
    }

    /**
     * Get hazelcast client config.
     * @param fileName
     * @return
     */
    public ClientConfig getClientConfig(final String fileName) {
        ClientConfig cfg = new XmlClientConfigBuilder(
                Cluster.class.getResourceAsStream(fileName)).build();
        cfg.setProperty("hazelcast.shutdownhook.policy", "GRACEFUL");
        return cfg;
    }

    /**
     * Get additional hazelcast system properties specified with -D option in
     * command line.
     * @return Properties
     */
    public Properties getHazelcastSystemProperties() {
        Properties properties = new Properties();
        System.getProperties().entrySet().stream()
                .filter(e -> ((String) e.getKey()).startsWith("hazelcast"))
                .forEach(e -> {
                    properties.setProperty((String) e.getKey(),
                            (String) e.getValue());
                    LOG.debug("set {} {}", e.getKey(), e.getValue());
                });
        return properties;
    }

}
