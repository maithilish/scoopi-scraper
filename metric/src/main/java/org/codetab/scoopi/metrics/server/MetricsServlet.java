package org.codetab.scoopi.metrics.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codetab.scoopi.metrics.aggregate.Aggregator;

public class MetricsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String CONTENT_TYPE = "application/json";
    private static final String AGGREGATOR = "scoopi.metricsAggregator";
    private static final String JSON_DATA = "scoopi.metricsJsonData";
    public static final String ALLOWED_ORIGIN =
            MetricsServlet.class.getCanonicalName() + ".allowedOrigin";

    protected String allowedOrigin;

    private Aggregator aggregator;

    public abstract static class ContextListener
            implements ServletContextListener {

        protected abstract Aggregator getMetricsAggregator();

        @Override
        public void contextInitialized(final ServletContextEvent event) {
            final ServletContext context = event.getServletContext();
            context.setAttribute(AGGREGATOR, getMetricsAggregator());
        }

        @Override
        public void contextDestroyed(final ServletContextEvent sce) {
        }

    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        final ServletContext context = config.getServletContext();
        if (null == aggregator) {
            final Object attr = context.getAttribute(AGGREGATOR);
            if (attr instanceof Aggregator) {
                aggregator = (Aggregator) attr;
                @SuppressWarnings("unchecked")
                Map<String, byte[]> metricsMap =
                        (Map<String, byte[]>) context.getAttribute(JSON_DATA);
                aggregator.setMetricsJsonData(metricsMap);
            } else {
                throw new ServletException(
                        "Couldn't find a MetricAggregator instance.");
            }
        }
        this.allowedOrigin = context.getInitParameter(ALLOWED_ORIGIN);
    }

    @Override
    protected void doGet(final HttpServletRequest req,
            final HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType(CONTENT_TYPE);
        if (allowedOrigin != null) {
            resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        }
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setStatus(HttpServletResponse.SC_OK);

        aggregator.aggregate();

        try (OutputStream output = resp.getOutputStream()) {
            output.write(aggregator.getJson());
        }
    }

}
