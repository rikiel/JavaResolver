package eu.profinit.manta.connector.java.analysis.mybatis.handler.configuration;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;

import eu.profinit.manta.connector.java.analysis.utils.Validate;

public class ConnectionConfiguration {
    private final String type;
    private final String connectionUrl;
    private final String userName;

    public ConnectionConfiguration(@Nonnull final String type,
                                   @Nonnull final String connectionUrl,
                                   @Nonnull final String userName) {
        Validate.notNullAll(type, connectionUrl, userName);
        this.type = type;
        this.connectionUrl = connectionUrl;
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("type", type)
                .add("connectionUrl", connectionUrl)
                .add("userName", userName)
                .toString();
    }
}
