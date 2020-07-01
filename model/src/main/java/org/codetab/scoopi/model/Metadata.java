package org.codetab.scoopi.model;

import java.io.Serializable;
import java.util.Date;

public class Metadata implements Serializable {

    private static final long serialVersionUID = -2163679498583421241L;

    private Fingerprint locator;
    private Fingerprint document;
    private Date documentDate;

    public Fingerprint getLocator() {
        return locator;
    }

    public void setLocator(final Fingerprint locator) {
        this.locator = locator;
    }

    public Fingerprint getDocument() {
        return document;
    }

    public void setDocument(final Fingerprint document) {
        this.document = document;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(final Date documentDate) {
        this.documentDate = documentDate;
    }

}
