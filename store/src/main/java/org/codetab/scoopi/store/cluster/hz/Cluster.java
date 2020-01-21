package org.codetab.scoopi.store.cluster.hz;

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
        LOGGER.info("start Hazelcast cluster");

        Config cfg = new XmlConfigBuilder(
                Cluster.class.getResourceAsStream("/hazelcast.xml")).build();
        cfg.addListenerConfig(new ListenerConfig(membershipListener));

        hz = Hazelcast.newHazelcastInstance(cfg);

        Set<Member> members = hz.getCluster().getMembers();
        LOGGER.info("joined Hazelcast cluster: group: {}, members: {}",
                cfg.getGroupConfig().getName(), members.size());
        for (Member member : members) {
            LOGGER.info("member: {}", member.getUuid());
        }
    }

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
}
