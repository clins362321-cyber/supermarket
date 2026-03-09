package org.example.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.UserAddressMapper;
import org.example.supermarket.dao.UserMapper;
import org.example.supermarket.dto.UserProfileDto;
import org.example.supermarket.dto.UserProfileUpdateDto;
import org.example.supermarket.entity.User;
import org.example.supermarket.entity.UserAddress;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, UserAddressMapper addressMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> list() {
        return userMapper.selectList(null);
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public User create(User user) {
        user.setId(null);
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.insert(user);
        return user;
    }

    public User register(User user) {
        user.setId(null);
        if (user.getUsername() == null || user.getUsername().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            return null;
        }
        Long cnt = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", user.getUsername())
        );
        if (cnt != null && cnt > 0) {
            return null;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    public User update(Long id, User user) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        existing.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(existing);
        return existing;
    }

    public boolean delete(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    public UserProfileDto getProfile(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
        if (user == null) {
            return null;
        }
        List<UserAddress> addresses = addressMapper.selectList(
                new QueryWrapper<UserAddress>()
                        .eq("username", username)
                        .orderByDesc("is_default", "id")
        );
        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setBalance(user.getBalance());
        dto.setAddresses(addresses);
        return dto;
    }

    public boolean updateProfile(String username, UserProfileUpdateDto payload) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
        if (user == null) {
            return false;
        }
        user.setFullName(payload.getFullName());
        user.setPhone(payload.getPhone());
        userMapper.updateById(user);
        return true;
    }

    public UserAddress saveAddress(String username, UserAddress addr) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        addr.setUsername(username);
        if (addr.getId() == null) {
            addressMapper.insert(addr);
        } else {
            addressMapper.updateById(addr);
        }
        return addr;
    }

    public boolean deleteAddress(Long id) {
        return addressMapper.deleteById(id) > 0;
    }
}
