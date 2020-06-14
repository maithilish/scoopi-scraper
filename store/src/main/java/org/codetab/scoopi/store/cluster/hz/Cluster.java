package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.store.ICluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

@Singleton
public class Cluster implements ICluster {

    static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    @Inject
    private MembershipListener membershipListener;

    private HazelcastInstance hz;

    @Override
    public void start() {
        String hzConfigFile = getConfigFile();
        try {
            LOGGER.info("load hz config file {}", hzConfigFile);
            Config cfg = new XmlConfigBuilder(
                    Cluster.class.getResourceAsStream(hzConfigFile)).build();
            cfg.addListenerConfig(new ListenerConfig(membershipListener));
            addSystemProperties(cfg);

            LOGGER.info("start Hazelcast cluster");
            hz = Hazelcast.newHazelcastInstance(cfg);

            String group = cfg.getGroupConfig().getName();
            logMemberInfo(group);
        } catch (IllegalArgumentException e) {
            LOGGER.error("hz config file {} not found", hzConfigFile);
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

    @Override
    public void shutdown() {
        // FIXME - bootfix, cluster shutdown vs node shutdown
        // hz.getCluster().shutdown();
        hz.shutdown();
    }

    @Override
    public Object getInstance() {
        return hz;
    }

    @Override
    public String getMemberId() {
        return hz.getLocalEndpoint().getUuid();
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
            return firstMember.get().getUuid();
        } else {
            throw new IllegalStateException("leader not found");
        }
    }

    @Override
    public Object getTxOptions(final Configs configs) {
        int txTimeout;
        try {
            txTimeout = Integer
                    .parseInt(configs.getConfig("scoopi.cluster.tx.timeout"));
            TimeUnit timeUnit = TimeUnit.valueOf(configs
                    .getConfig("scoopi.cluster.tx.timeoutUnit").toUpperCase());
            TransactionType txType = TransactionType.valueOf(
                    configs.getConfig("scoopi.cluster.tx.type").toUpperCase());
            return new TransactionOptions().setTransactionType(txType)
                    .setTimeout(txTimeout, timeUnit);
        } catch (NumberFormatException | ConfigNotFoundException e) {
            throw new CriticalException(e);
        }
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
                    LOGGER.debug("set {} {}", e.getKey(), e.getValue());
                });
    }

    private void logMemberInfo(final String group) {
        Set<Member> members = hz.getCluster().getMembers();
        LOGGER.info("joined Hazelcast cluster: group: {}, members: {}", group,
                members.size());
        for (Member member : members) {
            if (member.localMember()) {
                LOGGER.info("member: {} this", member.getUuid());
            } else {
                LOGGER.info("member: {}", member.getUuid());
            }
        }
    }

    @Override
    public int getSize() {
        return hz.getCluster().getMembers().size();
    }
}
