package org.codetab.scoopi.util;

import net.jpountz.lz4.LZ4CompressorWithLength;
import net.jpountz.lz4.LZ4DecompressorWithLength;
import net.jpountz.lz4.LZ4Factory;

public class LzCompressUtil {

    private static final LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    private LzCompressUtil() {
    }

    public static byte[] compress(final byte[] data) {
        LZ4CompressorWithLength compressor =
                new LZ4CompressorWithLength(FACTORY.fastCompressor());
        return compressor.compress(data);
    }

    public static byte[] decompress(final byte[] data) {
        LZ4DecompressorWithLength decompressor =
                new LZ4DecompressorWithLength(FACTORY.fastDecompressor());
        return decompressor.decompress(data);
    }
}
