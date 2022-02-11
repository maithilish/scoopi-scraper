package org.codetab.scoopi.metrics.server;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.codetab.scoopi.metrics.aggregate.Aggregator;
import org.codetab.scoopi.metrics.server.MetricsServlet.ContextListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ContextListenerTest {

    @InjectMocks
    private MyContextListener contextListener;

    private static Aggregator aggregator = Mockito.mock(Aggregator.class);

    public static class MyContextListener extends ContextListener {
        @Override
        protected Aggregator getMetricsAggregator() {
            return aggregator;
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testContextInitialized() {
        ServletContextEvent event = Mockito.mock(ServletContextEvent.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);

        String aggregatorKey = "scoopi.metricsAggregator";

        when(event.getServletContext()).thenReturn(servletContext);

        contextListener.contextInitialized(event);

        verify(servletContext).setAttribute(aggregatorKey, aggregator);
    }

    @Test
    public void testContextDestroyed() {
        ServletContextEvent event = Mockito.mock(ServletContextEvent.class);
        contextListener.contextDestroyed(event);
    }
}
