package org.codetab.scoopi.messages;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class MessagesTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetString() {
        assertThat(Messages.getString("ScoopiEngine.0"))
                .isEqualTo("start ScoopiEngine");
    }

    @Test
    public void testGetStringMissingKey() {
        assertThat(Messages.getString("ScoopiEngine.99"))
                .isEqualTo("!ScoopiEngine.99!");
    }
}
