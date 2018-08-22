package org.codetab.scoopi.step.parse;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.codetab.scoopi.model.Axis;

public class QueryVarSubstitutor {

    public void replaceVariables(final Map<String, String> queries,
            final Map<String, Axis> axisMap) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        // TODO provide some examples and full explanation in javadoc

        notNull(queries, "queries must not be null");
        notNull(axisMap, "axisMap must not be null");

        for (String key : queries.keySet()) {
            String str = queries.get(key);
            Map<String, String> valueMap = getValueMap(str, axisMap);
            StringSubstitutor ss = new StringSubstitutor(valueMap);
            ss.setVariablePrefix("%{"); //$NON-NLS-1$
            ss.setVariableSuffix("}"); //$NON-NLS-1$
            ss.setEscapeChar('%');
            String patchedStr = ss.replace(str);
            queries.put(key, patchedStr);
        }
    }

    /**
     * <p>
     * Get value map.
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
    private Map<String, String> getValueMap(final String str,
            final Map<String, ?> map) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        String[] keys = StringUtils.substringsBetween(str, "%{", "}"); //$NON-NLS-1$ //$NON-NLS-2$
        if (keys == null) {
            return null;
        }
        Map<String, String> valueMap = new HashMap<>();
        for (String key : keys) {
            String[] parts = key.split("\\."); //$NON-NLS-1$
            String objKey = parts[0];
            String property = parts[1];
            Object obj = map.get(objKey.toUpperCase());
            // call getter and type convert to String
            Object o = PropertyUtils.getProperty(obj, property);
            valueMap.put(key, ConvertUtils.convert(o));
        }
        return valueMap;
    }
}
