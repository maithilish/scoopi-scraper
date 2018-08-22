package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.codetab.scoopi.defs.yml.cache.AxisDefsCache;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.AxisName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AxisDefsTest {

    @Mock
    private AxisDefsCache axisDefsCache;

    @InjectMocks
    private AxisDefs axisDefs;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetQuery()
            throws IllegalAccessException, DataDefNotFoundException {

        String query = "test query";
        given(axisDefsCache.getQuery("defA", AxisName.COL, "region"))
                .willReturn(query);

        String actual = axisDefs.getQuery("defA", AxisName.COL, "region");

        assertThat(actual).isEqualTo(query);
    }
}
