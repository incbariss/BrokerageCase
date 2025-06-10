package task.ing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task.ing.model.dto.request.CustomerRequestDto;
import task.ing.model.dto.request.LoginRequestDto;
import task.ing.model.dto.response.CustomerResponseDto;
import task.ing.model.dto.response.LoginResponseDto;
import task.ing.service.CustomerService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "auth-controller")
public class AuthController {

    final AuthenticationManager authenticationManager;
    private final CustomerService customerService;

    @PostMapping("/register")
    @Operation(
            summary = "You must register before performing any actions",
            description = "Creating an USER role account.")
    public ResponseEntity<CustomerResponseDto> register(@RequestBody @Valid CustomerRequestDto dto) {
        CustomerResponseDto responseDto = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/login")
    @Operation(
            summary = "You can log in after registering.",
            description = "Log in before performing any actions. (Paste your token to the 'Authorize' button to log in)")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );

        LoginResponseDto response = customerService.login(dto.username());
        return ResponseEntity.ok(response);
    }


}
