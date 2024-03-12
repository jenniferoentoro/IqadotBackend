package iqadot.iqadotkit.config.security.token;

public interface AccessTokenEncoder {
    String encode(AccessToken accessToken);
}
