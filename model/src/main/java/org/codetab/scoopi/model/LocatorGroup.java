
package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LocatorGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private String group;
    private List<Locator> locators;
    /*
     * LocatorGroup defined by defs or created by parsed link default true
     * (defined by defs)
     */
    private boolean byDef = true;

    LocatorGroup() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public List<Locator> getLocators() {
        if (locators == null) {
            locators = new ArrayList<Locator>();
        }
        return locators;
    }

    public LocatorGroup copy() {
        LocatorGroup copy = new LocatorGroup();
        copy.setGroup(this.group);
        for (Locator locator : locators) {
            copy.getLocators().add(locator.copy());
        }
        return copy;
    }

    public boolean isByDef() {
        return byDef;
    }

    public void setByDef(final boolean byDef) {
        this.byDef = byDef;
    }

    @Override
    public boolean equals(final Object obj) {
        String[] excludes = {"id"};
        return EqualsBuilder.reflectionEquals(this, obj, excludes);
    }

    @Override
    public int hashCode() {
        String[] excludes = {"id"};
        return HashCodeBuilder.reflectionHashCode(this, excludes);
    }

    @Override
    public String toString() {
        return "LocatorGroup [group=" + group + ", locators=" + locators.size()
                + "]";
    }

}
