package org.codetab.scoopi.model;

import static java.util.Objects.isNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Metadata implements Serializable {

    private static final long serialVersionUID = -2163679498583421241L;

    private Fingerprint fingerprint; // id
    private Fingerprint locator;
    private Date documentDate;

    /*
     * data fingerprint - hash of locatorId, taskGroup, taskName, dataDef hash
     */
    private List<Fingerprint> data;

    public Fingerprint getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(final Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Fingerprint getLocator() {
        return locator;
    }

    public void setLocator(final Fingerprint locator) {
        this.locator = locator;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(final Date documentDate) {
        this.documentDate = documentDate;
    }

    public List<Fingerprint> getData() {
        return data;
    }

    public void addData(final Fingerprint dataFingerprint) {
        if (isNull(data)) {
            data = new ArrayList<>();
        }
        this.data.add(dataFingerprint);
    }

}
