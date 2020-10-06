package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.store.ICluster;

import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

@Singleton
public class Cluster implements ICluster {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private MembershipListener membershipListener;

    private HazelcastInstance hz;

    @Override
    public void start() {
        String hzConfigFile = getConfigFile();
        try {
            LOG.info("load hz config file {}", hzConfigFile);
            Config cfg = new XmlConfigBuilder(
                    Cluster.class.getResourceAsStream(hzConfigFile)).build();
            cfg.addListenerConfig(new ListenerConfig(membershipListener));
            addSystemProperties(cfg);

            LOG.info("start Hazelcast cluster");
            hz = Hazelcast.newHazelcastInstance(cfg);

            String group = cfg.getClusterName();
            logMemberInfo(group);
        } catch (IllegalArgumentException e) {
            LOG.error("hz config file {} not found", hzConfigFile);
            throw new CriticalException("fail to start Hazelcast cluster");
        }
    }

    /**
     * Get hazelcast config file name from system properties or return default
     * @return
     */
    private String getConfigFile() {
        String configFile = System.getProperty("hazelcast.config");
        if (isNull(configFile)) {
            configFile = "/hazelcast.xml";
        }
        return configFile;
    }

    /**
     * Called by Bootstrap wait for quorum to shutdown the cluster. Normal
     * shutdown is managed by ClusterShutdown.
     * <p>
     * Hz instance (node) shutdown is preferred as cluster shutdown fails
     * occasionally.
     */
    @Override
    public void shutdown() {
        hz.shutdown();
    }

    @Override
    public Object getInstance() {
        return hz;
    }

    @Override
    public String getMemberId() {
        return hz.getLocalEndpoint().getUuid().toString();
    }

    @Override
    public Map<String, byte[]> getMetricsHolder() {
        return hz.getMap("metrics");
    }

    @Override
    public String getLeader() {
        Optional<Member> firstMember =
                hz.getCluster().getMembers().stream().findFirst();
        if (firstMember.isPresent()) {
            return firstMember.get().getUuid().toString();
        } else {
            throw new IllegalStateException("leader not found");
        }
    }

    @Override
    public Object getTxOptions(final Configs configs) {
        int txTimeout;

        txTimeout = configs.getInt("scoopi.cluster.tx.timeout", "10");

        TimeUnit timeUnit = TimeUnit.valueOf(
                configs.getConfig("scoopi.cluster.tx.timeoutUnit", "SECONDS")
                        .toUpperCase());

        TransactionType txType = TransactionType.valueOf(
                configs.getConfig("scoopi.cluster.tx.type", "TWO_PHASE")
                        .toUpperCase());

        return new TransactionOptions().setTransactionType(txType)
                .setTimeout(txTimeout, timeUnit);
    }

    /**
     * Add additional hazelcast system properties specified with -D option in
     * command line
     * @param cfg
     */
    private void addSystemProperties(final Config cfg) {
        System.getProperties().entrySet().stream()
                .filter(e -> ((String) e.getKey()).startsWith("hazelcast"))
                .forEach(e -> {
                    cfg.setProperty((String) e.getKey(), (String) e.getValue());
                    LOG.debug("set {} {}", e.getKey(), e.getValue());
                });
    }

    private void logMemberInfo(final String group) {
        Set<Member> members = hz.getCluster().getMembers();
        LOG.info("joined Hazelcast cluster: group: {}, members: {}", group,
                members.size());
        for (Member member : members) {
            if (member.localMember()) {
                LOG.info("member: {} this", member.getUuid());
            } else {
                LOG.info("member: {}", member.getUuid());
            }
        }
    }

    @Override
    public int getSize() {
        return hz.getCluster().getMembers().size();
    }

    @Override
    public boolean isNodeRunning() {
        return hz.getLifecycleService().isRunning();
    }
}
