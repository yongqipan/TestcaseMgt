package com.pyq.tcm.auth;

import com.pyq.tcm.common.BizException;
import com.pyq.tcm.config.TokenStore;
import com.pyq.tcm.entity.User;
import com.pyq.tcm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenStore tokenStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenStore = tokenStore;
    }

    public Map<String, Object> login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new BizException(400, "请输入用户名和密码");
        }
        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BizException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        String token = tokenStore.issue(user.getId(), user.getUsername());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", toView(user));
        return result;
    }

    public Map<String, Object> currentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(401, "用户不存在"));
        return toView(user);
    }

    public void logout(String token) {
        tokenStore.revoke(token);
    }

    private Map<String, Object> toView(User user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("username", user.getUsername());
        view.put("nickname", user.getNickname());
        view.put("role", user.getRole());
        return view;
    }
}
