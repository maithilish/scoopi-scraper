package org.codetab.scoopi.defs.yml.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.codetab.scoopi.defs.yml.DataDefProvider;
import org.codetab.scoopi.defs.yml.helper.QueryHelper;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class QueryCacheTest {

    @Mock
    private QueryHelper queryHelper;
    @Mock
    private DataDefProvider dataDefProvider;

    @InjectMocks
    private QueryCache queryCache;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetQuery()
            throws IllegalAccessException, DataDefNotFoundException {
        String query = "test query";

        DataDef dataDefA = factory.createDataDef("defA", new Date(), new Date(),
                "defJsonA");
        given(dataDefProvider.getDataDef("defA")).willReturn(dataDefA);
        given(queryHelper.getQuery(dataDefA, AxisName.COL, "region"))
                .willReturn(query);

        // from def
        String actual = queryCache.getQuery("defA", AxisName.COL, "region");
        assertThat(actual).isEqualTo(query);
    }

    @Test
    public void testGetQueryFromCache()
            throws IllegalAccessException, DataDefNotFoundException {
        String query = "test query";

        DataDef dataDefA = factory.createDataDef("defA", new Date(), new Date(),
                "defJsonA");
        given(dataDefProvider.getDataDef("defA")).willReturn(dataDefA);
        given(queryHelper.getQuery(dataDefA, AxisName.COL, "region"))
                .willReturn(query);

        // from def
        String actual = queryCache.getQuery("defA", AxisName.COL, "region");
        assertThat(actual).isEqualTo(query);

        // from cache
        actual = queryCache.getQuery("defA", AxisName.COL, "region");
        assertThat(actual).isEqualTo(query);

        verify(queryHelper, times(1)).getQuery(dataDefA, AxisName.COL,
                "region");
    }
}
