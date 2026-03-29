package com.company.ems.auth.service;

import com.company.ems.auth.dto.AuthenticatedUserResponse;
import com.company.ems.auth.dto.LoginRequest;
import com.company.ems.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    AuthenticatedUserResponse getCurrentUser();
}
