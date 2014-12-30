package de.uhrenbastler.watchcheck.managers;

/**
 * Created by clorenz on 30.12.14.
 */
public class MigrationException extends Exception {

    public MigrationException() {
    }

    public MigrationException(String detailMessage) {
        super(detailMessage);
    }

    public MigrationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MigrationException(Throwable throwable) {
        super(throwable);
    }
}
