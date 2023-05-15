package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.response.CommonResponse;

public interface UserService {

    CommonResponse handleLogin(String phone,String password);

    CommonResponse handleRegister(String code, String phone, String password, Boolean gender,String nickName);
    CommonResponse handlePhoneLogin(String phone);

    CommonResponse handleCodeLogin(String phone, String code);

    CommonResponse handlePhoneRegister(String phone);

    CommonResponse handleEditInfo(String token,String id, String nickName,String phone);

    CommonResponse handleSaveNickName(String token,String nickName);

    CommonResponse handleChangePassword(String token, String oldPassword, String newPassword);

    CommonResponse findPasswordPhone(String phone);

    CommonResponse findPasswordCode(String phone ,String code, String password);

    void handleGameOver(boolean win, String userId);

    User findUserById(String userId);
}
