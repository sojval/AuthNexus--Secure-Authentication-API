package com.Sojval.Auth.Service;

import com.Sojval.Auth.Dtos.UserDto;
import com.Sojval.Auth.Entity.Provider;
import com.Sojval.Auth.Entity.User;
import com.Sojval.Auth.Exceptions.ResourceNotFoundException;
import com.Sojval.Auth.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.ReadOnlyFileSystemException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        if(userDto.getEmail() == null || userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is null or empty");
        }
        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        User user = modelMapper.map(userDto, User.class);
        user.setProvider(
                userDto.getProvider() != null ?
                        userDto.getProvider(): Provider.LOCAL);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found"));
        if(userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if(userDto.getImage() != null) {
            user.setImage(userDto.getImage());
        }
        if(userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }
        if(userDto.getProvider() != null) {
            user.setProvider(userDto.getProvider());
        }
        user.setEnabled(userDto.isEnabled());
        userRepository.save(user);
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Iterable<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user ->modelMapper.map(user,UserDto.class))
                .toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.getReferenceById(id);
        return modelMapper.map(user, UserDto.class);
    }
}
