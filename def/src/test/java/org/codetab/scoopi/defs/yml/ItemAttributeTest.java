package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Range;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Query;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ItemAttributeTest {

    @Test
    public void testBuilder() {
        ObjectFactory of = new ObjectFactory();
        String key = "tKey";
        Query query = of.createQuery();
        Range<Integer> indexRange = Range.between(10, 20);
        List<String> prefix = Lists.newArrayList("tPrefix");
        List<String> breakAfter = Lists.newArrayList("tBreakAfter");
        List<Filter> filter = Lists.newArrayList(of.createFilter("foo", "bar"));
        String linkGroup = "tLinkGroup";
        List<String> linkBreakOn = Lists.newArrayList("tLinkBreakOn");

        ItemAttribute actual = new ItemAttribute.Builder().setKey(key)
                .setQuery(query).setIndexRange(indexRange)
                .setBreakAfter(breakAfter).setPrefix(prefix).setFilter(filter)
                .setLinkGroup(linkGroup).setLinkBreakOn(linkBreakOn).build();

        assertThat(actual.getKey()).isEqualTo(key);
        assertThat(actual.getQuery()).isEqualTo(query);
        assertThat(actual.getIndexRange()).isEqualTo(indexRange);
        assertThat(actual.getPrefix()).isEqualTo(prefix);
        assertThat(actual.getBreakAfter()).isEqualTo(breakAfter);
        assertThat(actual.getFilter()).isEqualTo(filter);
        assertThat(actual.getLinkGroup()).isEqualTo(linkGroup);
        assertThat(actual.getLinkBreakOn()).isEqualTo(linkBreakOn);
    }

    @Test
    public void testBuilderCompare() {
        ItemAttribute actual = getItemAttribute();
        ItemAttribute another = getItemAttribute();

        assertThat(actual).isEqualTo(another);
        assertThat(actual.hashCode()).isEqualTo(another.hashCode());
    }

    @Test
    public void testBuilderToString() {
        ItemAttribute actual = getItemAttribute();

        String hex = new DigestUtils("SHA-1").digestAsHex(actual.toString());
        assertThat(hex).isEqualTo("5edc942dd111b911a7de25a58276dbd8cbd179e0");
    }

    public ItemAttribute getItemAttribute() {
        ObjectFactory of = new ObjectFactory();
        String key = "tKey";
        Query query = of.createQuery();
        Range<Integer> indexRange = Range.between(10, 20);
        List<String> prefix = Lists.newArrayList("tPrefix");
        List<String> breakAfter = Lists.newArrayList("tBreakAfter");
        List<Filter> filter = Lists.newArrayList(of.createFilter("foo", "bar"));
        String linkGroup = "tLinkGroup";
        List<String> linkBreakOn = Lists.newArrayList("tLinkBreakOn");

        return new ItemAttribute.Builder().setKey(key).setQuery(query)
                .setIndexRange(indexRange).setBreakAfter(breakAfter)
                .setPrefix(prefix).setFilter(filter).setLinkGroup(linkGroup)
                .setLinkBreakOn(linkBreakOn).build();
    }

}
