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
     * Topic prefix used to request claims
     */
    public static String REQUEST_CLAIMS_TOPIC_PREFIX = "access/claims";

    /**
     * The topic (within every restricted area) which is used to reply to claim requests
     */
    public static String REQUEST_CLAIMS_RESTRICTED_RESPONSE_TOPIC = "claims";

    /**
     * System topic for clients to request their topic (owned/involved).
     * The placeholder is replaced with the clientId
     */
    public static String REQUEST_CLAIMS_TOPIC_PATTERN = REQUEST_CLAIMS_TOPIC_PREFIX + "/%s/request";

    /**
     * System topic for clients to receive their claims (owned/involved).
     * The placeholder is replaced with the clientId
     */
    public static String REQUEST_CLAIMS_RESPONSE_TOPIC_PATTERN = REQUEST_CLAIMS_TOPIC_PREFIX + "/%s/" + REQUEST_CLAIMS_RESTRICTED_RESPONSE_TOPIC;


}
