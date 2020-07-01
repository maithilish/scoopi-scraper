package org.codetab.scoopi.model.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Fingerprint;

import com.google.common.hash.HashCode;

public class Fingerprints {

    private Fingerprints() {
    }

    public static Fingerprint fingerprint(final byte[]... inputs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            for (byte[] input : inputs) {
                digest.update(input);
            }
            // convert bytes to hex string
            String hex = HashCode.fromBytes(digest.digest()).toString();
            return new Fingerprint(hex);
        } catch (NoSuchAlgorithmException e) {
            throw new CriticalException(e);
        }
    }
}
