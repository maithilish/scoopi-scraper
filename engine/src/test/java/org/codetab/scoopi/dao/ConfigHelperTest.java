package org.codetab.scoopi.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.codetab.scoopi.config.ConfigService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigHelperTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigHelper configHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetOrmType() throws Exception {
        given(configService.getConfig("scoopi.datastore.orm")).willReturn("jdo")
                .willReturn("jDo").willReturn("jpa").willReturn("jPa")
                .willReturn(null);

        assertThat(configHelper.getOrmType()).isEqualTo(ORM.JDO);
        assertThat(configHelper.getOrmType()).isEqualTo(ORM.JDO);
        assertThat(configHelper.getOrmType()).isEqualTo(ORM.JPA);
        assertThat(configHelper.getOrmType()).isEqualTo(ORM.JPA);
        assertThat(configHelper.getOrmType()).isEqualTo(ORM.JDO);
    }

}
