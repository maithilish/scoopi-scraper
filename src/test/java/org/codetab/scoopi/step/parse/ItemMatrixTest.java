package org.codetab.scoopi.step.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.ItemMig;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.ItemHelper;
import org.codetab.scoopi.step.mig.parse.ItemMatrix;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ItemMatrixTest {

    @Mock
    private ItemHelper itemHelper;

    @InjectMocks
    private ItemMatrix itemMatrix;

    private ObjectFactory factory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        factory = new ObjectFactory();
    }

    @Test
    public void testCreateAdjacentItem() throws IllegalAccessException {
        Axis col = factory.createAxis(AxisName.COL, "date");
        col.setValue("c");
        col.setIndex(1);
        col.setOrder(2);

        Axis row = factory.createAxis(AxisName.ROW, "price");
        row.setValue("r");
        row.setIndex(10);
        row.setOrder(11);

        ItemMig itemMig = factory.createItemMig();
        itemMig.addAxis(col);
        itemMig.addAxis(row);

        // value is null in expected axis
        Axis expectedCol = factory.createAxis(AxisName.COL, "date");
        expectedCol.setIndex(2);
        expectedCol.setOrder(3);

        Axis expectedRow = factory.createAxis(AxisName.ROW, "price");
        expectedRow.setIndex(10);
        expectedRow.setOrder(11);

        ItemMig copy = itemMig.copy();

        String nextItemIndexesKey = "1020300";

        given(itemHelper.copy(itemMig)).willReturn(copy);

        assertThat(itemMatrix.notYetCreated(nextItemIndexesKey)).isTrue();

        ItemMig actual =
                itemMatrix.createAdjacentItem(itemMig, col, nextItemIndexesKey);

        assertThat(actual).isSameAs(copy);
        assertThat(actual.getAxis(AxisName.COL)).isEqualTo(expectedCol);
        assertThat(actual.getAxis(AxisName.ROW)).isEqualTo(expectedRow);

        assertThat(itemMatrix.notYetCreated(nextItemIndexesKey)).isFalse();
    }

    @Test
    public void testNotYetCreated() throws IllegalAccessException {

        String nextItemIndexesKey = "1020300";

        @SuppressWarnings("unchecked")
        Set<String> createdSet = (Set<String>) FieldUtils.readField(itemMatrix,
                "createdSet", true);

        assertThat(itemMatrix.notYetCreated(nextItemIndexesKey)).isTrue();

        createdSet.add(nextItemIndexesKey);

        assertThat(itemMatrix.notYetCreated(nextItemIndexesKey)).isFalse();
    }

}
