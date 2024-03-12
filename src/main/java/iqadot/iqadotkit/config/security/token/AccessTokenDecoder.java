package iqadot.iqadotkit.config.security.token;

public interface AccessTokenDecoder {
    AccessToken decode(String accessTokenEncoded);
}
