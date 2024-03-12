package iqadot.iqadotkit.config.security.token;

import java.util.*;

public interface AccessToken {
    String getSubject();

    Long getUserId();

}
