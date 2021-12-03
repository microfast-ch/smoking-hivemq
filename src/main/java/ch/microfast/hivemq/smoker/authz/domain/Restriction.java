package ch.microfast.hivemq.smoker.authz.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * A {@link Restriction} holds the owner and describes the list of permission which is applied on the claimed topic.
 * The serialized form of this class is signed by the client which is held in the outer claim.
 */
public class Restriction implements Serializable {

    private static final long serialVersionUID = 69L;

    /**
     * The Topic to be claimed in the restricted area. The this topic is prefixed with 'restricted/{clientId}/'.
     */
    @NotEmpty
    private String topicName;

    /**
     * The restriction type that defines if the permissions are interpreted as a blacklist or whitelist. Permissions change their meanings if this value is changed.
     */
    @NotNull
    private RestrictionType restrictionType;

    /**
     * The list of defined permissions in the context of this restriction. Permissions change their meanings if the restrictionType is changed.
     *
     * <p>
     * NOTE: If a permission gets more complex maybe consider taking {@link com.hivemq.extension.sdk.api.auth.parameter.TopicPermission} directly here...
     * </p>
     */
    @NotNull
    private Collection<Permission> permissions;


    public Restriction() {
        permissions = new ArrayList<>();
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public RestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public void addPermission(Permission... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restriction that = (Restriction) o;
        return topicName.equals(that.topicName) &&
                restrictionType == that.restrictionType &&
                permissions.equals(that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, restrictionType, permissions);
    }

    @Override
    public String toString() {
        return "Restriction{" +
                ", topicName='" + topicName + '\'' +
                ", restrictionType=" + restrictionType +
                ", permissions=" + permissions +
                '}';
    }

    @JsonIgnore
    public String getOwner() {
        return this.topicName.split("/")[1];
    }
}
