package org.codetab.scoopi.metrics.server;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.metrics.aggregate.Aggregator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MetricsServletTest {

    @InjectMocks
    private MetricsServlet metricsServlet;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() throws Exception {
        ServletConfig config = Mockito.mock(ServletConfig.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        Aggregator aggregator = Mockito.mock(Aggregator.class);
        Map<String, byte[]> metricsMap = new HashMap<>();
        String orange = "Foo";

        String aggregatorKey = "scoopi.metricsAggregator";
        String jsonDataKey = "scoopi.metricsJsonData";
        String allowedOriginKey =
                MetricsServlet.class.getCanonicalName() + ".allowedOrigin";

        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute(aggregatorKey)).thenReturn(aggregator);
        when(context.getAttribute(jsonDataKey)).thenReturn(metricsMap);
        when(context.getInitParameter(allowedOriginKey)).thenReturn(orange);

        metricsServlet.init(config);

        verify(aggregator).setMetricsJsonData(metricsMap);
    }

    @Test
    public void testInitThrowsException() throws Exception {
        ServletConfig config = Mockito.mock(ServletConfig.class);
        ServletContext context = Mockito.mock(ServletContext.class);

        Object aggregator = Mockito.mock(Object.class); // trigger exception

        Map<String, byte[]> metricsMap = new HashMap<>();
        String orange = "Foo";

        String aggregatorKey = "scoopi.metricsAggregator";
        String jsonDataKey = "scoopi.metricsJsonData";
        String allowedOriginKey =
                MetricsServlet.class.getCanonicalName() + ".allowedOrigin";

        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute(aggregatorKey)).thenReturn(aggregator);
        when(context.getAttribute(jsonDataKey)).thenReturn(metricsMap);
        when(context.getInitParameter(allowedOriginKey)).thenReturn(orange);

        assertThrows(ServletException.class, () -> metricsServlet.init(config));
    }

    @Test
    public void testInitAggregatorIsSet() throws Exception {
        ServletConfig config = Mockito.mock(ServletConfig.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        Aggregator aggregator = Mockito.mock(Aggregator.class);
        Map<String, byte[]> metricsMap = new HashMap<>();
        String orange = "Foo";

        String allowedOriginKey =
                MetricsServlet.class.getCanonicalName() + ".allowedOrigin";

        FieldUtils.writeDeclaredField(metricsServlet, "aggregator", aggregator,
                true);

        when(config.getServletContext()).thenReturn(context);
        when(context.getInitParameter(allowedOriginKey)).thenReturn(orange);

        metricsServlet.init(config);

        verify(aggregator, never()).setMetricsJsonData(metricsMap);
    }

    @Test
    public void testDoGet() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream output = Mockito.mock(ServletOutputStream.class);
        byte[] grape = {};

        Aggregator aggregator = Mockito.mock(Aggregator.class);

        String contentTypeKey = "application/json";
        String allowedOriginKey =
                MetricsServlet.class.getCanonicalName() + ".allowedOrigin";

        FieldUtils.writeDeclaredField(metricsServlet, "aggregator", aggregator,
                true);
        FieldUtils.writeDeclaredField(metricsServlet, "allowedOrigin",
                allowedOriginKey, true);

        when(resp.getOutputStream()).thenReturn(output);
        when(aggregator.getJson()).thenReturn(grape);

        metricsServlet.doGet(req, resp);

        verify(resp).setContentType(contentTypeKey);
        verify(resp).setHeader("Access-Control-Allow-Origin", allowedOriginKey);
        verify(resp).setHeader("Cache-Control",
                "must-revalidate,no-cache,no-store");
        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(aggregator).aggregate();
        verify(output).write(grape);
    }

    @Test
    public void testDoGetAllowedOriginIsNull() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream output = Mockito.mock(ServletOutputStream.class);
        byte[] grape = {};

        Aggregator aggregator = Mockito.mock(Aggregator.class);

        String contentTypeKey = "application/json";
        String allowedOriginKey =
                MetricsServlet.class.getCanonicalName() + ".allowedOrigin";

        FieldUtils.writeDeclaredField(metricsServlet, "aggregator", aggregator,
                true);
        // allowedOrigin is not set

        when(resp.getOutputStream()).thenReturn(output);
        when(aggregator.getJson()).thenReturn(grape);

        metricsServlet.doGet(req, resp);

        verify(resp).setContentType(contentTypeKey);
        verify(resp, never()).setHeader("Access-Control-Allow-Origin",
                allowedOriginKey);
        verify(resp).setHeader("Cache-Control",
                "must-revalidate,no-cache,no-store");
        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(aggregator).aggregate();
        verify(output).write(grape);
    }
}
