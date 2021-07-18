package org.codetab.scoopi.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import net.jpountz.lz4.LZ4CompressorWithLength;
import net.jpountz.lz4.LZ4Factory;

public class LzCompressUtilTest {

    private static final LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testCompress() {
        byte[] data = RandomStringUtils.randomAlphanumeric(1000).getBytes();

        LZ4CompressorWithLength compressor =
                new LZ4CompressorWithLength(FACTORY.fastCompressor());
        byte[] expected = compressor.compress(data);

        byte[] actual = LzCompressUtil.compress(data);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testDecompress() {
        byte[] data = RandomStringUtils.randomAlphanumeric(1000).getBytes();

        byte[] compressed = LzCompressUtil.compress(data);
        byte[] actual = LzCompressUtil.decompress(compressed);

        assertThat(actual).isEqualTo(data);
    }

}
