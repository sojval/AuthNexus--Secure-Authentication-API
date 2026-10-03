package com.Sojval.Auth.Service;

import com.Sojval.Auth.Dtos.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserByEmail(String email);
    UserDto updateUser(UserDto userDto,Long id);
    void deleteUser(Long id);
    Iterable<UserDto> getAllUsers();
    UserDto getUserById(Long id);
}
