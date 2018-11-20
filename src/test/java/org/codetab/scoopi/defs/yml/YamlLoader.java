package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlLoader {

    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static JsonNode load(final String file) throws IOException {
        try (InputStream ymlStream =
                YamlLoader.class.getResourceAsStream(file)) {
            if (isNull(ymlStream)) {
                throw new FileNotFoundException(file);
            }
            return mapper.readTree(ymlStream);
        }
    }

}
