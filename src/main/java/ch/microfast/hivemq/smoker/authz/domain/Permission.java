package ch.microfast.hivemq.smoker.authz.domain;

import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * The {@link Permission} defines which client is allowed to perform which MQTT activity.
 */
public class Permission implements Serializable {

    private static final long serialVersionUID = 69L;

    /**
     * The clientId which is addressed with this permission
     */
    @NotEmpty
    private String clientId;

    /**
     * The MQTT activity such as subscribe, publish or both
     */
    @NotNull
    private TopicPermission.MqttActivity activity;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public TopicPermission.MqttActivity getActivity() {
        return activity;
    }

    public void setActivity(TopicPermission.MqttActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return clientId.equals(that.clientId) &&
                activity == that.activity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, activity);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "identifier='" + clientId + '\'' +
                ", activity=" + activity +
                '}';
    }
}
