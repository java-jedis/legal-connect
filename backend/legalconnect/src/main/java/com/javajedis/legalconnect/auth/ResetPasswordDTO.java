package com.javajedis.legalconnect.auth;

import com.javajedis.legalconnect.common.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "OTP must not be blank")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 characters")
    private String otp;
}
