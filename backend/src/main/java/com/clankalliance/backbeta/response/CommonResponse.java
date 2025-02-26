package com.clankalliance.backbeta.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    //业务成功与失败
    private Boolean success = true;
    //返回信息
    private String message;
    //返回泛型数据 自定义类型
    private T content;

    //若为null, 登录失效
    private String token;

}
