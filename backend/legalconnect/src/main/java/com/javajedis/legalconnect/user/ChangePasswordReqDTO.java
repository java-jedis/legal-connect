package com.javajedis.legalconnect.user;

import com.javajedis.legalconnect.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordReqDTO {

    @NotBlank(message = "Old Password is required")
    private String oldPassword;

    @NotBlank(message = "New Password can not be empty")
    @ValidPassword
    private String password;
}
