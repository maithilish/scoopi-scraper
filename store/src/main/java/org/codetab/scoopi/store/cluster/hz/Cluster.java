package org.codetab.scoopi.store.cluster.hz;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.store.ICluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

@Singleton
public class Cluster implements ICluster {

    static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    @Inject
    private MembershipListener membershipListener;

    private HazelcastInstance hz;

    @Override
    public boolean start() {
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
        return true;
    }

    @Override
    public boolean shutdown() {
        hz.shutdown();
        return true;
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

}
