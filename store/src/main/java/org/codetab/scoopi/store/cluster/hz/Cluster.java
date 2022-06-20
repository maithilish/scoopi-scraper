package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.store.ICluster;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

@Singleton
public class Cluster implements ICluster {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private MembershipListener membershipListener;
    @Inject
    private HazelcastConfig hazelcastConfig;
    @Inject
    private Factory factory;

    private HazelcastInstance hz;

    @Override
    public void start(final String clusterMode, final String configFileName) {

        Properties properties = hazelcastConfig.getHazelcastSystemProperties();

        String configFile = configFileName;
        try {
            // create server or client hazelcast instance
            if (clusterMode.equalsIgnoreCase("server")) {
                // default server config
                if (isNull(configFile)) {
                    configFile = "/hazelcast.xml";
                }

                LOG.info("load hazelcast config file {}", configFile);
                Config cfg = hazelcastConfig.getConfig(configFile);
                cfg.addListenerConfig(
                        factory.createListenerConfig(membershipListener));

                // add system and other properties
                properties.forEach(
                        (k, v) -> cfg.setProperty((String) k, (String) v));

                LOG.info("start Hazelcast cluster");
                hz = factory.createHazelcastInstance(cfg);

                logMemberInfo(cfg.getClusterName());

            } else {
                // default client config
                if (isNull(configFile)) {
                    configFile = "/hazelcast-client.xml";
                }

                LOG.info("load hazelcast client config file {}", configFile);
                ClientConfig clientCfg =
                        hazelcastConfig.getClientConfig(configFile);
                clientCfg.addListenerConfig(
                        factory.createListenerConfig(membershipListener));

                // unlike server, at present no system properties are added to
                // client, enable if required later

                LOG.info("start Hazelcast client");
                hz = factory.createHazelcastClientInstance(clientCfg);

                logMemberInfo(clientCfg.getClusterName());
            }
        } catch (IllegalArgumentException e) {
            LOG.error("hz config file {} not found", configFile);
            String message = "fail to start Hazelcast client";
            if (clusterMode.equalsIgnoreCase("server")) {
                message = "fail to start Hazelcast cluster";
            }
            throw new CriticalException(message);
        }
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
    public String getShortId(final String memberId) {
        return memberId.substring(memberId.lastIndexOf("-") + 1);
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

        return factory.createTxOptions().setTransactionType(txType)
                .setTimeout(txTimeout, timeUnit);
    }

    @Override
    public int getSize() {
        return hz.getCluster().getMembers().size();
    }

    @Override
    public boolean isNodeRunning() {
        return hz.getLifecycleService().isRunning();
    }

    private void logMemberInfo(final String group) {
        Set<Member> members = hz.getCluster().getMembers();

        Optional<Member> thisMember =
                members.stream().filter(Member::localMember).findFirst();
        if (thisMember.isPresent()) {
            LOG.info("joined Hazelcast cluster: group: {}, members: {}", group,
                    members.size());
            LOG.info("this member address: {}",
                    thisMember.get().getSocketAddress());
            LOG.info("this member uuid: {} this", thisMember.get().getUuid());
        }

        for (Member member : members) {
            if (!member.localMember()) {
                LOG.info("member: {}", member.getUuid());
            }
        }
    }
}
