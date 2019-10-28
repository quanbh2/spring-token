package net.friend.controller;

import net.friend.model.CustomUserDetails;
import net.friend.model.LoginRequest;
import net.friend.model.LoginResponse;
import net.friend.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class TestController {
  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenProvider tokenProvider;

  @PostMapping("/login")
  public LoginResponse authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    // Authentication with username and password
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

    // If there's no exception, set Security Context with authentication
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // Return jwt to user
    String jwt = tokenProvider.generateToken((CustomUserDetails) authentication.getPrincipal());
    return new LoginResponse(jwt);
  }

  // Api /api/random require authentication to access
  @GetMapping("/random")
  public String randomStuff() {
    return "Valid jwt";
  }

  @GetMapping("/logout")
  public void logout() {
  }
}
