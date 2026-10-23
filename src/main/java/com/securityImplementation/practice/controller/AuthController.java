package com.securityImplementation.practice.controller;
import com.securityImplementation.practice.dtos.LoginRequest;
import com.securityImplementation.practice.dtos.LoginResponse;
import com.securityImplementation.practice.dtos.RegisterRequest;
import com.securityImplementation.practice.service.AuthService;
import com.securityImplementation.practice.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authService=authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()));

        String token = jwtService.generateToken(request.username());

        return  ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request){

        authService.register(request.username(),request.password());
        return ResponseEntity.ok().build();
    }


}
