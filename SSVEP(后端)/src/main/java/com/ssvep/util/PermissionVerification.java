package com.ssvep.util;

import com.ssvep.dto.UserDto;
import com.ssvep.service.UserService;

import javax.servlet.ServletException;

public class PermissionVerification {
    private static UserService userService;

    public void init() throws ServletException {
        userService = new UserService();
    }

    public static boolean verifiedByID(Long userid) {
        UserDto userDto = userService.getUserById(userid);
        UserDto.Role UserRole = userDto.getRole();
        return UserRole.toString().equals("USER");
    }
}
