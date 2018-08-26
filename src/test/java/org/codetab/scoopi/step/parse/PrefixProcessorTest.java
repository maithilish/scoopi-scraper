package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.defs.IAxisDefs;
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

import com.google.common.collect.Lists;

public class PrefixProcessorTest {

    @Mock
    private IAxisDefs axisDefs;

    @InjectMocks
    private PrefixProcessor prefixProcessor;

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
    public void testGetPrefixes() {
        Date now = new Date();
        DataDef dataDef = factory.createDataDef("price", now, now, "def");
        AxisName axisName = AxisName.COL;

        Optional<List<String>> prefixes =
                Optional.of(Lists.newArrayList("test"));

        given(axisDefs.getPrefixes(dataDef, axisName)).willReturn(prefixes);

        Optional<List<String>> actual =
                prefixProcessor.getPrefixes(dataDef, axisName);

        assertThat(actual).isEqualTo(prefixes);
    }

    @Test
    public void testPrefixValue() {
        List<String> prefixes = Collections
                .unmodifiableList(Lists.newArrayList("p1", "p2", "p3"));

        String actual = prefixProcessor.prefixValue("val", prefixes);

        assertThat(actual).isEqualTo("p3p2p1val");
    }

    @Test
    public void testPrefixValuesNullParams() {
        try {
            prefixProcessor.prefixValue(null, new ArrayList<>());
            fail("should throw exception");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("value must not be null");
        }

        try {
            prefixProcessor.prefixValue("value", null);
            fail("should throw exception");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("prefixes must not be null");
        }
    }

}
