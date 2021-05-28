package ch.microfast.hivemq.smoker.authz.domain;

/**
 * The type to define how {@link Permission} are interpreted within a {@link Restriction}
 */
public enum RestrictionType {
    WHITELIST,
    BLACKLIST
}
