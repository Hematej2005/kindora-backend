package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.dto.AuthResponse;
import com.kindora.kindora_backend.dto.LoginRequest;
import com.kindora.kindora_backend.dto.RegisterRequest;
import com.kindora.kindora_backend.model.User;
import com.kindora.kindora_backend.repository.UserRepository;
import com.kindora.kindora_backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    // cookie name used by frontend script.js
    private static final String COOKIE_NAME = "kindora_token";

    /**
     * Add a Set-Cookie header with SameSite attribute. We build the Set-Cookie header manually
     * because the javax.servlet.Cookie API does not allow setting SameSite in all servlet containers.
     *
     * @param res response
     * @param token token value (empty string to clear)
     * @param maxAgeSeconds max-age in seconds (0 to delete)
     */
    private void addAuthCookieHeader(HttpServletResponse res, String token, int maxAgeSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append(COOKIE_NAME).append("=").append(token == null ? "" : token);
        sb.append("; Path=/");
        sb.append("; Max-Age=").append(maxAgeSeconds);
        sb.append("; HttpOnly");
        // For local dev keep Secure off. In production (https) set Secure and consider SameSite=None if cross-site.
        sb.append("; SameSite=Lax");
        res.addHeader("Set-Cookie", sb.toString());
    }

    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req, HttpServletResponse res) {

        if (req.getFullName() == null || req.getEmail() == null || req.getPassword() == null) {
            return new AuthResponse(false, "Full name, email and password required");
        }

        if (repo.findByEmailIgnoreCase(req.getEmail()).isPresent()) {
            return new AuthResponse(false, "Email already registered");
        }

        User u = new User();
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setRole(req.getRole());
        u.setPhoneNumber(req.getPhoneNumber());
        repo.save(u);

        // generate token and set cookie for 7 days
        String token = jwtService.generateToken(u.getEmail());
        addAuthCookieHeader(res, token, 7 * 24 * 60 * 60);

        return new AuthResponse(true, "Registration successful");
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req, HttpServletResponse res) {

        Optional<User> opt = repo.findByEmailIgnoreCase(req.getEmail());
        if (opt.isEmpty()) return new AuthResponse(false, "Invalid email or password");

        User u = opt.get();

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            return new AuthResponse(false, "Invalid email or password");
        }

        String token = jwtService.generateToken(u.getEmail());
        addAuthCookieHeader(res, token, 7 * 24 * 60 * 60);

        return new AuthResponse(true, "Login successful");
    }

    // ---------------- GET LOGGED-IN USER ----------------
    // Note: we read cookie server-side inside JwtAuthenticationFilter for security,
    // but for this endpoint we manually read cookie from request (Spring will bind it).
    @GetMapping("/me")
    public User me(@CookieValue(name = COOKIE_NAME, required = false) String token) {
        if (token == null || token.isBlank()) return null;
        if (!jwtService.isValid(token)) return null;

        String email = jwtService.extractEmail(token);
        return repo.findByEmailIgnoreCase(email).orElse(null);
    }

    // ---------------- LOGOUT ----------------
    @PostMapping("/logout")
    public AuthResponse logout(HttpServletResponse res) {
        // clear cookie by setting empty value and Max-Age=0
        addAuthCookieHeader(res, "", 0);
        return new AuthResponse(true, "Logout successful");
    }
}
