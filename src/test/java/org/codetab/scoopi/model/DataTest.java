package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class DataTest {

    private Data data;

    @Before
    public void setUp() throws Exception {
        data = new Data();
    }

    @Test
    public void testData() {
        assertThat(data.getMembers()).isNotNull();
    }

    @Test
    public void testGetId() {
        data.setId(1L);
        assertThat(data.getId()).isEqualTo(1L);
    }

    @Test
    public void testGetName() {
        data.setName("x");
        assertThat(data.getName()).isEqualTo("x");
    }

    @Test
    public void testGetDataDef() {
        data.setDataDef("x");
        assertThat(data.getDataDef()).isEqualTo("x");
    }

    @Test
    public void testGetDataDefId() {
        data.setDataDefId(10L);
        assertThat(data.getDataDefId()).isEqualTo(10L);
    }

    @Test
    public void testGetDocumentId() {
        data.setDocumentId(20L);
        assertThat(data.getDocumentId()).isEqualTo(20L);
    }

    @Test
    public void testGetMembers() {
        Member m1 = new Member();
        m1.setName("m1");
        Member m2 = new Member();
        m2.setName("m2");
        Data data1 = new Data();
        data1.setName("data");

        List<DataComponent> members = Lists.newArrayList(data1, m1, m2);
        data.setMembers(members);

        assertThat(data.getMembers()).containsOnly(m1, m2);
    }

    @Test
    public void testAddMember() {
        Member member = new Member();
        member.setName("x");
        member.setGroup("y");

        List<DataComponent> members = new ArrayList<>();
        data.setMembers(members);
        data.addMember(member);

        assertThat(data.getMembers()).contains(member);
    }

    @Test
    public void testToStringIds() {
        data.setId(1L);
        data.setDataDefId(2L);
        data.setDocumentId(3L);

        String expected = "Data [id=1, dataDefId=2, documentId=3]";

        assertThat(data.toStringIds()).isEqualTo(expected);
    }

    @Test
    public void testHashCode() {
        data.setName("x");
        data.setDataDef("d");
        data.setDataDefId(2L);
        data.setDocumentId(3L);
        assertThat(data.hashCode()).isEqualTo(1408814739L);
    }

    @Test
    public void testEquals() {
        data.setId(1L);
        data.setName("x");
        data.setDataDef("d");
        data.setDataDefId(2L);
        data.setDocumentId(3L);

        Data data2 = new Data();
        data2.setId(12L); // id may be different
        data2.setName("x");
        data2.setDataDef("d");
        data2.setDataDefId(2L);
        data2.setDocumentId(3L);

        assertThat(data).isEqualTo(data2);
    }
}
