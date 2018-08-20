package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.codetab.scoopi.defs.yml.cache.QueryCache;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.AxisName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class QueryProviderTest {

    @Mock
    private QueryCache queryCache;

    @InjectMocks
    private QueryProvider queryProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetQuery()
            throws IllegalAccessException, DataDefNotFoundException {

        String query = "test query";
        given(queryCache.getQuery("defA", AxisName.COL, "region"))
                .willReturn(query);

        String actual = queryProvider.getQuery("defA", AxisName.COL, "region");

        assertThat(actual).isEqualTo(query);
    }
}
