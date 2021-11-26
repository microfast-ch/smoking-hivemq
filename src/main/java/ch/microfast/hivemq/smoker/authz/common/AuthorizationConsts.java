package ch.microfast.hivemq.smoker.authz.common;

public final class AuthorizationConsts {
    /**
     * Value uses to allow any client on a claim
     */
    public static String ANY_CLIENT_IDENTIFIER = "*";

    /**
     * Topic name where clients can submit claims to
     */
    public static String CLAIM_TOPIC = "access/claim";

    /**
     * Topic name where clients can submit unclaims to
     */
    public static String UNCLAIM_TOPIC = "access/unclaim";

    /**
     * Topic segment name that builds the first node of the restricted area topic tree
     */
    public static String RESTRICTED_AREA_PREFIX = "restricted";

    /**
     * System topic for clients to request their topic (owned/involved)
     */
    public static String REQUEST_CLAIMS_TOPIC_PATTERN = "access/claims/%s/request";
}
