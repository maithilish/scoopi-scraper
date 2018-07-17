package org.codetab.scoopi.model;

public final class Log {

    public enum CAT {
        ERROR, CONFIG, FATAL, INTERNAL, USER
    };

    private CAT cat;
    private String label;
    private String message;
    private Throwable throwable;

    // TODO remove this
    public Log(final CAT cat, final String message) {
        super();
        this.cat = cat;
        this.message = message;
    }

    // TODO remove this
    public Log(final CAT cat, final String message, final Throwable throwable) {
        this.cat = cat;
        this.message = message;
        this.throwable = throwable;
    }

    public Log(final CAT cat, final String label, final String message) {
        this.cat = cat;
        this.message = message;
        this.label = label;
    }

    public Log(final CAT cat, final String label, final String message,
            final Throwable throwable) {
        this.cat = cat;
        this.message = message;
        this.label = label;
        this.throwable = throwable;
    }

    public CAT getCat() {
        return cat;
    }

    public String getLabel() {
        return label;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Log [type=");
        sb.append(cat);
        sb.append(" label=");
        sb.append(label);
        sb.append(" message=");
        sb.append(message);
        sb.append("]");
        if (throwable != null) {
            sb.append(System.lineSeparator());
            sb.append("          throwable=");
            sb.append(throwable);
        }
        return sb.toString();
    }
}
