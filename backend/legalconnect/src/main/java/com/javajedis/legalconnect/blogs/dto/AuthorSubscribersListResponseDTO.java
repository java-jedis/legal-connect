package com.javajedis.legalconnect.blogs.dto;

import java.util.List;

import com.javajedis.legalconnect.user.UserInfoResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorSubscribersListResponseDTO {
    private List<UserInfoResponseDTO> subscribers;
} 