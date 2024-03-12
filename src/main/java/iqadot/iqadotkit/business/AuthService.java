package iqadot.iqadotkit.business;

import iqadot.iqadotkit.controller.domain.*;

public interface AuthService {
    LoginResp login(LoginReq loginRequest);
}
