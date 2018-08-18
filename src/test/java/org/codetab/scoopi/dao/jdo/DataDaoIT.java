package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;

import javax.jdo.JDODataStoreException;

import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Member;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class DataDaoIT extends ITBase {

    private DataDao dao;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        dao = new DataDao(daoUtil.getPersistenceManagerFactory());

        daoUtil.clearCache();
        schemaClasses.add("org.codetab.scoopi.model.Data");
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @Test
    public void testStoreData() {
        Data data = createTestData();
        dao.storeData(data);

        List<Data> actuals = daoUtil.getObjects(Data.class,
                Lists.newArrayList("detachMembers"));

        assertThat(actuals.size()).isEqualTo(1);
        assertThat(actuals.get(0)).isEqualTo(data);
    }

    @Test
    public void testStoreDataShouldThrowException() {
        Data data = createTestData();
        dao.storeData(data);

        // violate UNIQUE_DATA constraint
        testRule.expect(JDODataStoreException.class);
        dao.storeData(data);
    }

    @Test
    public void testStoreDataNullParams() {
        try {
            dao.storeData(null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("data must not be null");
        }
    }

    @Test
    public void testGetDataByDocumentIdAndDataDefId() {
        long documentId = 10L;
        long dataDefId = 11L;

        Data data = createTestData();
        dao.storeData(data);

        Data data1 = createTestData();
        data1.setDocumentId(documentId);
        data1.setDataDefId(dataDefId);
        dao.storeData(data1);

        Data actual = dao.getData(documentId, dataDefId);

        assertThat(actual).isEqualTo(data1);
    }

    @Test
    public void testGetDataByDocumentIdAndDataDefIdNotFound() {
        long documentId = 10L;
        long dataDefId = 11L;

        Data data = createTestData();
        dao.storeData(data);

        Data actual = dao.getData(documentId, dataDefId);

        assertThat(actual).isNull();
    }

    @Test
    public void testGetDataByDocumentIdAndDataDefIdShouldThrowException()
            throws SQLException {
        daoUtil.dropConstraint(daoUtil.getPersistenceManagerFactory(), "data",
                "UNIQUE_DATA");

        Data data = createTestData();
        dao.storeData(data);
        dao.storeData(data); // duplicate entry

        testRule.expect(IllegalStateException.class);
        dao.getData(1L, 2L);
    }

    @Test
    public void testGetDataLong() {
        Data data = createTestData();
        dao.storeData(data);

        Data actual = dao.getData(data.getId());

        assertThat(actual).isEqualTo(data);
    }

    private Data createTestData() {
        Axis col = objectFactory.createAxis(AxisName.COL);
        Member member = objectFactory.createMember();
        member.setName("date");
        member.setGroup("group1");
        member.getAxes().add(col);
        Data data = objectFactory.createData("dataDef1");
        data.setName("acme");
        data.setDocumentId(1L);
        data.setDataDefId(2L);
        data.addMember(member);
        return data;
    }
}
