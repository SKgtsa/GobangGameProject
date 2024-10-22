package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.repository.UserRepository;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.TencentService;
import com.clankalliance.backbeta.service.UserService;
import com.clankalliance.backbeta.utils.ErrorHandle;
import com.clankalliance.backbeta.utils.SnowFlake;
import com.clankalliance.backbeta.utils.StatusManipulateUtilsWithRedis.ManipulateUtilRedis;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.StringJoiner;

@Service
public class UserServiceImpl implements UserService {

    public final String USER_CLASS_NAME = "用户";

    public final String DEFAULT_AVATAR_URL = "/static/inbuild/default.jpg";

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserRepository userRepository;

    @Resource
    private ManipulateUtilRedis ManipulateUtil;

    @Resource
    private TencentService tencentService;

    @Resource
    private SnowFlake snowFlake;

    @Override
    public CommonResponse handleLogin(String phone,String password) {
        CommonResponse  response = new CommonResponse<>();
        response.setSuccess(false);
        password = DigestUtils.sha1Hex(password);
        Optional<User> uop = userRepository.findByPhone(phone);
        if(uop.isEmpty()){
            response.setSuccess(false);
            response.setMessage("账户不存在");
            return response;
        }
        User user = uop.get();
        if(!password.equals(user.getPassword())){
            response.setMessage("密码错误");
            return response;
        }
        response.setSuccess(true);
        response.setMessage("登陆成功");
        response.setToken(ManipulateUtil.updateStatus(user.getId()));
        return response;
    }

    /**
     * 注册方法
     * @param code 手机验证码
     * @param phone 手机
     * @param password 密码
     * @param gender 性别
     * @param nickName 昵称
     * @return
     */
    @Override
    public CommonResponse handleRegister(String code, String phone, String password,Boolean gender,String nickName) {
        password = DigestUtils.sha1Hex(password.getBytes());
        CommonResponse  response = new CommonResponse();
        Optional<User> uop = userRepository.findByPhone(phone);
        if(uop.isPresent()){
            //该用户已存在 进行判定是统一注册的空号还是用户重复注册
            response.setSuccess(false);
            response.setMessage("账户已存在");
            return response;
        }
        String id = ManipulateUtil.getIdByPhone(phone, code);
        if(id == null){
            //验证码错误
            response.setSuccess(false);
            response.setMessage("短信验证码错误");
            return response;
        }
        User user = new User(id, nickName, phone, gender, password, 0,0,DEFAULT_AVATAR_URL);
        try{
            userRepository.save(user);
        }catch (Exception e){
            return ErrorHandle.handleSaveException(e, response);
        }
        response.setSuccess(true);
        response.setMessage("注册成功");
        response.setToken(ManipulateUtil.updateStatus(id));
        return response;
    }

    /**
     * 手机登录 发送验证码方法
     * @param phone 手机号
     * @return
     */
    @Override
    public CommonResponse handlePhoneLogin(String phone){
        CommonResponse response = new CommonResponse();
        Optional<User> uop = userRepository.findByPhone(phone);
        if(uop.isPresent()){
            User user = uop.get();
            String code = ManipulateUtil.updatePhoneStatus(user.getId(), phone);
            tencentService.sendSms(phone,code);
            response.setSuccess(true);
            response.setMessage("已发送验证码");
        }else{
            response.setMessage("用户不存在");
            response.setSuccess(false);
        }
        return response;
    }

    /**
     * 手机登录 验证验证码 生成token
     * @param phone 手机号
     * @param code 验证码
     * @return
     */
    @Override
    public CommonResponse handleCodeLogin(String phone, String code){
        return tokenUtil.phoneCodeCheck(phone,code);
    }

    /**
     * 注册时发送验证码
     * @param phone 手机号
     * @return
     */
    @Override
    public CommonResponse handlePhoneRegister(String phone){
        CommonResponse response = new CommonResponse();
        Optional<User> uop = userRepository.findByPhone(phone);
        if(uop.isPresent()){
            //手机已被绑定
            response.setSuccess(false);
            response.setMessage("手机已被绑定");
        }else{
            String id = snowFlake.nextId() + "";
            String code = ManipulateUtil.updatePhoneStatus(id, phone);
            tencentService.sendSms(phone,code);
            response.setSuccess(true);
            response.setMessage("已发送验证码");
        }
        return response;
    }


    @Override
    public CommonResponse handleEditInfo(String token,String id, String nickName,String phone){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getSuccess())
            return response;
        Optional<User> uop = userRepository.findById(id);
        if(uop.isEmpty()){
            response.setSuccess(false);
            response.setMessage("用户不存在");
            return response;
        }
        User user = uop.get();
        user.setNickName(nickName);
        user.setPhone(phone);
        try{
            userRepository.save(user);
        }catch (Exception e){
            return ErrorHandle.handleSaveException(e,response);
        }
        response.setMessage("保存成功");
        return response;
    }

    /**
     * 修改昵称
     * @param token 用户令牌
     * @param nickName 新昵称
     * @return
     */
    @Override
    public CommonResponse handleSaveNickName(String token,String nickName){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getSuccess())
            return response;
        Optional<User> uop = userRepository.findById(response.getMessage());
        if(uop.isEmpty()){
            return ErrorHandle.handleNotExist(USER_CLASS_NAME, response);
        }
        User user = uop.get();
        user.setNickName(nickName);
        try{
            userRepository.save(user);
        }catch (Exception e){
            return ErrorHandle.handleSaveException(e, response);
        }
        response.setMessage("保存成功");
        return response;
    }

    /**
     * 修改密码
     * @param token
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @Override
    public CommonResponse handleChangePassword(String token, String oldPassword, String newPassword){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getSuccess())
            return response;
        User user = userRepository.findById(response.getMessage()).get();
        oldPassword = DigestUtils.sha1Hex(oldPassword);
        newPassword = DigestUtils.sha1Hex(newPassword);
        if(!oldPassword.equals(user.getPassword())){
            response.setSuccess(false);
            response.setMessage("密码错误");
            return response;
        }
        user.setPassword(newPassword);
        try{
            userRepository.save(user);
        }catch (Exception e){
            return ErrorHandle.handleSaveException(e, response);
        }
        return response;
    }


    /**
     * 通过手机找回密码 发送验证码
     * @param phone 电话号码
     * @return
     */
    @Override
    public CommonResponse findPasswordPhone(String phone){
        CommonResponse response = new CommonResponse<>();
        Optional<User> uop = userRepository.findByPhone(phone);
        if(uop.isEmpty())
            return ErrorHandle.handleNotExist(USER_CLASS_NAME, response);
        User user = uop.get();
        String code = ManipulateUtil.updatePhoneStatus(user.getId(), phone);
        tencentService.sendSms(phone, code);
        response.setMessage("发送成功");
        return response;
    }

    /**
     * 验证手机验证码并修改密码
     * @param phone 电话号码
     * @param code 验证码
     * @param password 新密码
     * @return
     */
    @Override
    public CommonResponse findPasswordCode(String phone ,String code, String password){
        CommonResponse response = tokenUtil.phoneCodeCheck(phone,code);
        if(!response.getSuccess())
            return response;
        User user = userRepository.findById(response.getMessage()).get();
        password = DigestUtils.sha1Hex(password.getBytes());
        user.setPassword(password);
        userRepository.save(user);
        response.setMessage("修改成功");
        return response;
    }

    /**
     * 根据游戏结果更新游戏记录
     * @param win
     * @param userId
     */
    @Override
    public String handleGameOver(boolean win, String userId){
        Optional<User> uop = userRepository.findById(userId);
        if(uop.isEmpty())
            return null;
        User user = uop.get();
        if(win){
            user.setWinNum(user.getWinNum() + 1);
        }else{
            user.setLoseNum(user.getLoseNum() + 1);
        }
        userRepository.save(user);
        StringJoiner sj = new StringJoiner("?");
        sj.add("connected");
        sj.add(user.getNickName());
        sj.add("" + user.getWinNum());
        sj.add("" + user.getLoseNum());
        sj.add("" + user.getId());
        sj.add("" + user.isGender());
        sj.add(user.getAvatarURL());
        return sj.toString();
    }

    /**
     * 为提高耦合性而实现的查找用户方法
     * @param userId
     * @return
     */
    @Override
    public User findUserById(String userId){
        Optional<User> uop = userRepository.findById(userId);
        return uop.orElse(null);
    }
}
