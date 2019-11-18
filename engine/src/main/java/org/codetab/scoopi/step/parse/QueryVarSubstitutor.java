package org.codetab.scoopi.step.parse;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.codetab.scoopi.model.Axis;

import com.google.common.collect.Lists;

public class QueryVarSubstitutor {

    /**
     * <p>
     * Substitutes variables such as ${col.match} in each query with the value
     * from corresponding axis field.
     * </p>
     *
     * <pre>
     * Format of variables is ${AxisName.FieldName}
     * Examples:
     *   ${col.index}, ${col.match}, ${col.value}
     *   ${row.index}, ${row.match}, ${row.value}
     * </pre>
     *
     * @param queries
     * @param varValueMap
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public void replaceVariables(final Map<String, String> queries,
            final Map<String, String> varValueMap)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        // TODO provide some examples and full explanation in javadoc

        notNull(queries, "queries must not be null");
        notNull(varValueMap, "varValueMap must not be null");

        for (String key : queries.keySet()) {
            String query = queries.get(key);
            StringSubstitutor ss = new StringSubstitutor(varValueMap);
            ss.setVariablePrefix("%{"); //$NON-NLS-1$
            ss.setVariableSuffix("}"); //$NON-NLS-1$
            ss.setEscapeChar('%');
            String patchedQuery = ss.replace(query);
            queries.put(key, patchedQuery);
        }
    }

    /**
     * <p>
     * Returns map of variable name and its value.
     * </p>
     * <p>
     * for variables such as %{index} or %{item.index} etc., value from ownAxis
     * is returned
     * </p>
     *
     * for variables such as %{item.xyz.index} etc., value from axis whose
     * itemName is xyz is returned
     *
     * <pre>
     * Key              Value
     * index            8
     * item.match       Price
     * dim.xyz.value    20.00
     * </pre>
     *
     * @param str
     *            string to parse
     * @param map
     *            axis map
     * @return axis value map
     * @throws IllegalAccessException
     *             on error
     * @throws InvocationTargetException
     *             on error
     * @throws NoSuchMethodException
     *             on error
     */
    public Map<String, String> getVarValueMap(final Map<String, String> queries,
            final List<Axis> axisList, final Axis ownAxis)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        List<String> placeHolders = new ArrayList<>();
        for (String query : queries.values()) {
            String[] ph = StringUtils.substringsBetween(query, "%{", "}"); //$NON-NLS-1$ //$NON-NLS-2$
            if (nonNull(ph)) {
                placeHolders.addAll(Lists.newArrayList(ph));
            }
        }
        Map<String, String> valueMap = new HashMap<>();
        for (String placeHolder : placeHolders) {
            String[] parts = placeHolder.split("\\."); //$NON-NLS-1$
            Axis axis = null;
            String property = null;

            int len = parts.length;
            final int one = 1;
            final int two = 2;
            final int three = 3;
            switch (len) {
            case one:
                // example: index
                property = parts[0];
                axis = ownAxis;
                break;
            case two:
                // example: item.index
                String axisName = parts[0];
                property = parts[1];
                axis = axisList.stream()
                        .filter(a -> a.getAxisName().equals(axisName))
                        .findFirst().orElse(null);
                break;
            case three:
                // example: item.Price.index
                axisName = parts[0];
                String itemName = parts[1];
                property = parts[2];
                axis = axisList.stream()
                        .filter(a -> a.getAxisName().equals(axisName)
                                && a.getItemName().equals(itemName))
                        .findFirst().orElse(null);
                break;
            default:
                break;
            }
            // call getter and type convert to String
            Object o = PropertyUtils.getProperty(axis, property);
            valueMap.put(placeHolder, ConvertUtils.convert(o));
        }
        return valueMap;
    }
}
