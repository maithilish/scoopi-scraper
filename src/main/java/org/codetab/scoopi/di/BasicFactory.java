package org.codetab.scoopi.di;

import org.codetab.scoopi.pool.PoolStat;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.inject.assistedinject.Assisted;

public interface BasicFactory {

    Server getServer(@Assisted("port") int port);

    WebAppContext getWebAppContext();

    PoolStat getPoolStat();
}
