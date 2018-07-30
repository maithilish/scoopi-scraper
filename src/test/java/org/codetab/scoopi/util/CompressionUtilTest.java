package org.codetab.scoopi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * CompressionUtil Tests.
 * @author Maithilish
 *
 */
public class CompressionUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUtilityClassWellDefined()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        TestUtils.assertUtilityClassWellDefined(CompressionUtil.class);
    }

    @Test
    public void testCompressByteArray() throws IOException {
        byte[] input = String.valueOf("test string").getBytes();

        byte[] actual = CompressionUtil.compressByteArray(input, 4096);

        byte[] expected = compress(input);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testCompressByteArrayNullParams() throws IOException {
        try {
            CompressionUtil.compressByteArray(null, 4096);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("input must not be null");
        }
    }

    @Test
    public void testDecompressByteArray()
            throws IOException, DataFormatException {
        byte[] expected = String.valueOf("test string").getBytes();

        byte[] input = compress(expected);

        byte[] actual = CompressionUtil.decompressByteArray(input, 4096);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testDecompressByteArrayNullParams()
            throws IOException, DataFormatException {
        try {
            CompressionUtil.decompressByteArray(null, 4096);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("input must not be null");
        }
    }

    @Test
    public void testCompressDecompress()
            throws IOException, DataFormatException {
        byte[] expected = String.valueOf("test string").getBytes();

        byte[] compressed = CompressionUtil.compressByteArray(expected, 4096);
        byte[] actual = CompressionUtil.decompressByteArray(compressed, 4096);

        assertThat(actual).isEqualTo(expected);
    }

    private byte[] compress(final byte[] input) throws IOException {
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }

        compressor.end();
        bos.close();
        return bos.toByteArray();
    }
}
