package org.codetab.scoopi.itest.fin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.codetab.scoopi.helper.HttpHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpHelperIT {

    private HttpHelper httpHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() {
        httpHelper = new HttpHelper();
    }

    @Test
    public void getContentTest() throws IOException {
        byte[] expected = getExpectedContent();
        byte[] actual = httpHelper.getContent("http://localhost/testpage.html",
                "Mozilla", 12000);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getContentMovedPageTest() throws IOException {
        byte[] expected = getExpectedContent();
        byte[] actual = httpHelper.getContent("http://localhost/movedpage.html",
                "Mozilla", 12000);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getContentNoPageTest() throws IOException {

        testRule.expect(IOException.class);
        httpHelper.getContent("http://localhost/unknownpage.html", "Mozilla",
                12000);

    }

    private byte[] getExpectedContent() {
        /*
         * <html> <body> test content </body> </html>
         */
        String nl = System.lineSeparator();
        StringBuffer sb = new StringBuffer();
        sb.append("<html>").append(nl).append(" <body>").append(nl)
                .append("  test content").append(nl).append(" </body>")
                .append(nl).append("</html>").append(nl);
        return sb.toString().getBytes();
    }
}
