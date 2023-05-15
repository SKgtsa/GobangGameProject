package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.request.user.*;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.UserService;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserService userService;


    /*
    每一个CommonResponse都要返回一个character
     */

    @PostMapping("/login")
    public CommonResponse login(@RequestBody UserLoginRequest request){
        return userService.handleLogin(request.getPhone(),request.getPassword());
    }

    @PostMapping("/register")
    public CommonResponse register(@RequestBody UserSaveRequest request){
        return userService.handleRegister(request.getCode(), request.getPhone(), request.getPassword(), request.isGender(), request.getNickName());
    }

    @PostMapping("/tokenCheck")
    public CommonResponse tokenCheck(@RequestBody TokenCheckRequest request){
        return tokenUtil.tokenCheck(request.getToken());
    }

    @PostMapping("/loginPhone")
    public CommonResponse handlePhoneLogin(@RequestBody PhoneCheckRequest request){
        return userService.handlePhoneLogin(request.getPhone());
    }

    @PostMapping("/loginCode")
    public CommonResponse handleCodeLogin(@RequestBody PhoneCheckRequest request){
        return userService.handleCodeLogin(request.getPhone(), request.getCode());
    }

    @PostMapping("/registerPhone")
    public CommonResponse handlePhoneRegister(@RequestBody PhoneCheckRequest request){
        return userService.handlePhoneRegister(request.getPhone());
    }

//    @PostMapping("/myInfo")
//    public CommonResponse handleGetInfo(@RequestBody TokenCheckRequest request){
//        return userService.handleGetInfo(request.getToken());
//    }

    //aaa
    @PostMapping("/editInfo")
    public CommonResponse handleEditInfo(@RequestBody UserSaveRequest request){
        return userService.handleEditInfo(request.getToken(),request.getId(), request.getNickName(), request.getPhone());
    }

    @PostMapping("/editNickName")
    public CommonResponse handleEditNickName(@RequestBody UserSaveRequest request){
        return userService.handleSaveNickName(request.getToken(),request.getNickName());
    }

//    @PostMapping("/changeAvatar")
//    public CommonResponse handleChangeAvatar(HttpSession session,
//                                             // 路径变量 解决前后端不一致
//                                             @RequestParam("avatar") MultipartFile avatar, @RequestParam("token") String token){
//        return avatarService.handleSave(avatar,token);
//    }

    @PostMapping("/changePassword")
    public CommonResponse handleChangePassword(@RequestBody PasswordChangeRequest request){
        return userService.handleChangePassword(request.getToken(),request.getOldPassword(),request.getNewPassword());
    }

    @PostMapping("/findPasswordPhone")
    public CommonResponse findPasswordPhone(@RequestBody FindPasswordRequest request){
        return userService.findPasswordPhone(request.getPhone());
    }

    @PostMapping("/findPasswordCode")
    public CommonResponse findPasswordCode(@RequestBody FindPasswordRequest request){
        return userService.findPasswordCode(request.getPhone(), request.getCode(), request.getPassword());
    }

}
