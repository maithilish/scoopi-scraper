package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;

public class JacksonsTest {

    private Jacksons jacksons;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        jacksons = new Jacksons();
        mapper = new ObjectMapper(new JsonFactory());
    }

    @Test
    public void testGetFieldNamesJsonNode()
            throws JsonMappingException, JsonProcessingException {
        String json = getTestJson();
        JsonNode node = mapper.readTree(json);

        List<String> actual = jacksons.getFieldNames(node);

        assertThat(actual).containsExactly("bar", "foo");
    }

    @Test
    public void testGetFieldNamesJsonNodeStringArray()
            throws JsonMappingException, JsonProcessingException {
        String json = getTestJson();
        JsonNode node = mapper.readTree(json);

        List<String> actual = jacksons.getFieldNames(node, "foo");
        assertThat(actual).containsExactly("name", "contact");

        actual = jacksons.getFieldNames(node, "foo", "contact");
        assertThat(actual).containsExactly("tel", "email");
    }

    @Test
    public void testGetFieldValue() throws JsonMappingException,
            JsonProcessingException, DefNotFoundException {
        String json = getTestJson();
        JsonNode node = mapper.readTree(json);

        String actual = jacksons.getFieldValue(node, "foo", "name");
        assertThat(actual).isEqualTo("foo");
    }

    @Test
    public void testGetFieldValueMissingNode() throws JsonMappingException,
            JsonProcessingException, DefNotFoundException {
        String json = getTestJson();
        JsonNode node = mapper.readTree(json);

        assertThrows(DefNotFoundException.class,
                () -> jacksons.getFieldValue(node, "foo", "address"));
    }

    @Test
    public void testPath() {
        String actual = jacksons.path("foo", "bar");

        assertThat(actual).isEqualTo("/foo/bar");
    }

    @Test
    public void testFindByField()
            throws JsonMappingException, JsonProcessingException {
        String json = getTestJson();
        List<JsonNode> nodes = Lists.newArrayList(mapper.readTree(json));

        JsonNode actual = jacksons.findByField(nodes, "name", "foo").get();

        String expected =
                "{\"name\":\"foo\",\"contact\":{\"tel\":\"123\",\"email\":\"foo@ex.in\"}}";
        assertThat(actual.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetArrayAsStrings()
            throws JsonMappingException, JsonProcessingException {
        String str = "{ name: foo, tel :[ 123, 456 ], email: foo@ex.in }";
        String json = JsonParser.parseString(str).toString();
        JsonNode node = mapper.readTree(json);

        List<String> actual = jacksons.getArrayAsStrings(node, "tel");

        assertThat(actual).containsExactly("123", "456");
    }

    @Test
    public void testGetArrayAsStringsNoTArray()
            throws JsonMappingException, JsonProcessingException {
        String str = "{ name: foo, tel :[ 123, 456 ], email: foo@ex.in }";
        String json = JsonParser.parseString(str).toString();
        JsonNode node = mapper.readTree(json);

        List<String> actual = jacksons.getArrayAsStrings(node, "name");

        assertThat(actual).isNull();
    }

    @Test
    public void testGetArrayAsStringsNullName()
            throws JsonMappingException, JsonProcessingException {
        String str = "{ name: foo, tel :[ 123, 456 ], email: foo@ex.in }";
        String json = JsonParser.parseString(str).toString();
        JsonNode node = mapper.readTree(json);

        List<String> actual = jacksons.getArrayAsStrings(node, null);

        assertThat(actual).isNull();
    }

    @Test
    public void testParseJson() {
        String actual = jacksons.parseJson("[{item: {name: fact}}]");

        assertThat(actual).isEqualTo("[{\"item\":{\"name\":\"fact\"}}]");
    }

    private String getTestJson() throws JsonProcessingException {
        Person foo = new Person("foo", new Contact("123", "foo@ex.in"));
        Person bar = new Person("bar", new Contact("456", "bar@ex.in"));

        Map<String, Person> persons = new HashMap<>();
        persons.put("foo", foo);
        persons.put("bar", bar);
        return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(persons);
    }

}

class Person {

    private final String name;
    private final Contact contact;

    Person(final String name, final Contact contact) {
        this.name = name;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public Contact getContact() {
        return contact;
    }
}

class Contact {
    private final String tel;
    private final String email;

    Contact(final String tel, final String email) {
        this.tel = tel;
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public String getEmail() {
        return email;
    }
}
