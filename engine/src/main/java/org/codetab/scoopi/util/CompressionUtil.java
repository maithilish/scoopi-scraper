package org.codetab.scoopi.util;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * <p>
 * Utility methods to compress and decompress byte array.
 * @author Maithilish
 *
 */
public final class CompressionUtil {

    /**
     * private constructor.
     */
    private CompressionUtil() {
    }

    /**
     * <p>
     * Compress byte array.
     *
     * @param input
     *            byte array to compress
     * @param bufferLength
     *            length of buffer
     * @return compressed byte array
     * @throws IOException
     *             if error closing stream
     */
    public static byte[] compressByteArray(final byte[] input,
            final int bufferLength) throws IOException {
        notNull(input, "input must not be null");
        // bufferLength is int, so it can't be null

        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[bufferLength];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }

        compressor.end();
        bos.close();

        return bos.toByteArray();
    }

    /**
     * <p>
     * Decompress byte array.
     *
     * @param input
     *            byte array to compress
     * @param bufferLength
     *            length of buffer
     * @return uncompressed byte array
     * @throws IOException
     *             if error closing stream
     * @throws DataFormatException
     *             if error in data format
     */
    public static byte[] decompressByteArray(final byte[] input,
            final int bufferLength) throws DataFormatException, IOException {
        notNull(input, "input must not be null");
        // bufferLength is int, so it can't be null

        final Inflater decompressor = new Inflater();

        decompressor.setInput(input);

        // Create an expandable byte array to hold the decompress data
        final ByteArrayOutputStream baos =
                new ByteArrayOutputStream(input.length);

        final byte[] buf = new byte[bufferLength];

        while (!decompressor.finished()) {
            int count = decompressor.inflate(buf);
            baos.write(buf, 0, count);
        }

        decompressor.end();
        baos.close();

        return baos.toByteArray();
    }
}
