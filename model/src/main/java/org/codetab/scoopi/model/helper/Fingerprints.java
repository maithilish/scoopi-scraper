package org.codetab.scoopi.model.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.codetab.scoopi.exception.CriticalException;

import com.google.common.hash.HashCode;

public class Fingerprints {

    private Fingerprints() {
    }

    public static String fingerprint(final byte[]... inputs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            for (byte[] input : inputs) {
                digest.update(input);
            }
            // convert bytes to hex string
            return HashCode.fromBytes(digest.digest()).toString();
        } catch (NoSuchAlgorithmException e) {
            throw new CriticalException(e);
        }
    }
}
