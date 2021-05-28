package ch.microfast.hivemq.smoker.authz.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A {@link Claim} represents a topic claim made by a client. The claim wraps the {@link Restriction} which is signed by the client.
 * The broker should never modify the restriction as the signature and thus the claim becomes invalid.
 */
public class Claim implements Serializable {

    private static final long serialVersionUID = 69L;

    /**
     * The signature of the serialized form of the restriction.
     */
    @NotEmpty
    private String signature;

    /**
     * The {@link Restriction} that holds owner and permission information for the claimed topic.
     */
    @NotNull
    private Restriction restriction;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Restriction getRestriction() {
        return restriction;
    }

    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return signature.equals(claim.signature) &&
                restriction.equals(claim.restriction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, restriction);
    }

    @Override
    public String toString() {
        return "Claim{" +
                ", signature='" + signature + '\'' +
                ", restriction=" + restriction +
                '}';
    }
}
