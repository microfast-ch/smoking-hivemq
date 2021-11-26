package ch.microfast.hivemq.smoker.authz.common;


import org.apache.commons.lang3.StringUtils;

public class TopicHelper {
    /// <summary>
    ///     Checks if the topic is reserved. Means topics which cannot be subscribed and only published if client is SMOKER authenticated
    /// </summary>
    /// <param name="topic"></param>
    public static boolean IsReservedTopic(String topic) {
        return AuthorizationConsts.CLAIM_TOPIC.equals(topic) || AuthorizationConsts.UNCLAIM_TOPIC.equals(topic);
    }

    /// <summary>
    ///     Checks if the first topic segment is equal to the restricted area prefix
    /// </summary>
    /// <param name="topic">The Topic to check</param>
    public static boolean IsTopicInRestrictedArea(String topic) {
        return TopicSegmentIsEqualTo(topic, 0, AuthorizationConsts.RESTRICTED_AREA_PREFIX);
    }

    /// <summary>
    ///     Checks if the second topic segment is equal to the owner ID / client ID
    /// </summary>
    /// <param name="topic">The topic to check</param>
    /// <param name="owner">the owner / client ID to check</param>
    public static boolean IsTopicOwnedByOwner(String topic, String owner) {
        return TopicSegmentIsEqualTo(topic, 1, owner);
    }

    public static boolean TopicSegmentIsEqualTo(String topic, int segmentIdx, String expectedValue) {
        if (topic == null) {
            throw new IllegalArgumentException("topic must not be null or empty");
        }

        String trimmedTopic = StringUtils.stripStart(topic, "/");
        String[] segments = topic.split("/");

        if (segments.length - 1 < segmentIdx) {
            throw new IndexOutOfBoundsException("The given topic:=" + topic + " does not contain enough segments");
        }

        return expectedValue.equals(segments[segmentIdx]);
    }


}
