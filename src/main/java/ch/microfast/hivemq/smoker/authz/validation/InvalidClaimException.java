package ch.microfast.hivemq.smoker.authz.validation;

import java.util.Collection;
import java.util.Collections;

/**
 * Exception
 */
public class InvalidClaimException extends Exception {

    private final Collection<String> errorMessages;

    public InvalidClaimException(Collection<String> errorMessages) {
        this.errorMessages = Collections.unmodifiableCollection(errorMessages);
    }

    public Collection<String> getErrorMessages() {
        return errorMessages;
    }
}
