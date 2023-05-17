package com.clankalliance.backbeta.utils;

import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.utils.StatusManipulateUtilsWithRedis.ManipulateUtilRedis;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class TokenUtil {


    @Resource
    private ManipulateUtilRedis manipulateUtil;


    /**
     * 验证token是否有效，同时返回新token及用户对象
     * @param token 前台传来的token
     * @return
     */
    public CommonResponse tokenCheck(String token){
        CommonResponse response = new CommonResponse();
        String id =  manipulateUtil.findStatusByToken(token);
        if(id != null){
            response.setSuccess(true);
            response.setToken(manipulateUtil.updateStatus(id));
            response.setMessage(id);
        }else{
            response.setSuccess(false);
            response.setMessage("登录失效");
        }
        return response;
    }

    public CommonResponse phoneCodeCheck(String phone, String code){
        CommonResponse response = new CommonResponse();
        String id = manipulateUtil.getIdByPhone(phone, code);
        if(id == null){
            response.setMessage("验证码不正确");
            response.setSuccess(false);
        }else{
            response.setSuccess(true);
            response.setToken(manipulateUtil.updateStatus(id));
            response.setMessage(id);
        }
        return response;
    }

}
