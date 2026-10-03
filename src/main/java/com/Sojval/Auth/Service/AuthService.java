package com.Sojval.Auth.Service;

import com.Sojval.Auth.Dtos.UserDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    UserDto registerUser( UserDto userDto);
}
