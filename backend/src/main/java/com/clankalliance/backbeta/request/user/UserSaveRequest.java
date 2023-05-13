package com.clankalliance.backbeta.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSaveRequest {

    private String code;

    private String phone;

    private String password;

    private boolean gender;

    private String nickName;

    private String token;

    private String id;

}
