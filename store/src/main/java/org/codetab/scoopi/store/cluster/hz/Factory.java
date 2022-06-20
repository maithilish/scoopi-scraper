package org.codetab.scoopi.store.cluster.hz;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionOptions;

public class Factory {

    public HazelcastInstance createHazelcastInstance(final Config cfg) {
        return Hazelcast.newHazelcastInstance(cfg);
    }

    public HazelcastInstance createHazelcastClientInstance(
            final ClientConfig clientCfg) {
        return HazelcastClient.newHazelcastClient(clientCfg);
    }

    public ListenerConfig createListenerConfig(
            final MembershipListener membershipListener) {
        return new ListenerConfig(membershipListener);
    }

    public TransactionOptions createTxOptions() {
        return new TransactionOptions();
    }
}
