package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.GeneralUploadService;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.UUID;

@Service
public class GeneralUploadServiceImpl implements GeneralUploadService {

    // 设置上传文件的最大值
    public static final int FILE_MAX_SIZE = 30*1024*1024;

    @Resource
    private TokenUtil tokenUtil;


    @Override
    public CommonResponse handleSave(MultipartFile file, String token){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getSuccess())
            return response;
        // 文件是否为空
        if (file.isEmpty()) {
            System.out.println("文件为空");
        }
        if (file.getSize() > FILE_MAX_SIZE) {
            System.out.println("文件大小超出限制");
        }
        //暂时不做类型检查
//        // 文件类型是否符合
//        String contentType = file.getContentType();
//        // 如果集合包含某元素则返回ture
//        if (!AVATAR_TYPE.contains(contentType)) {
//            System.out.println("文件类型不支持");
//        }
        // 上传的文件.../upload/file/文件
        String parent =
                System.getProperty("user.dir") + "/static/file";
        // File对象指向这个路径，file是否存在
        File dir = new File(parent);
        if (!dir.exists()) { // 检测目录是否存在
            dir.mkdirs(); // 创建当前目录
        }
        // 获取到这个文件名称 uuid工具来将生成一个新的字符串作为文件名
        // 例如：avatar01.png
        String originalFilename = file.getOriginalFilename();
        System.out.println("Originalfilename" + originalFilename);
        // 截取文件后缀
        String suffix = "";
        int index = originalFilename.lastIndexOf(".");
        suffix = originalFilename.substring(index);
        // 随机生成前缀名并拼接
        String filename = UUID.randomUUID().toString().toUpperCase() + suffix;
        File dest = new File(dir, filename); // 是一个空文件
        // 参数file中数据写入到这个空文件中
        try {
            file.transferTo(dest); //将file文件中的数据写入到dest文件中
        }
        catch (Exception e) {
            System.out.println("文件状态异常或文件读写异常");
        }

        // 返回文件的路径/upload/test.png
        String filePath = "/static/file/" + filename;
        response.setMessage("上传成功");
        response.setContent(filePath);
        return response;
    }

    @Override
    public String upload(MultipartFile file){
        // 文件是否为空
        if (file.isEmpty()) {
            System.out.println("文件为空");
        }
        if (file.getSize() > FILE_MAX_SIZE) {
            System.out.println("文件大小超出限制");
        }
        //暂时不做类型检查
//        // 文件类型是否符合
//        String contentType = file.getContentType();
//        // 如果集合包含某元素则返回ture
//        if (!AVATAR_TYPE.contains(contentType)) {
//            System.out.println("文件类型不支持");
//        }
        // 上传的文件.../upload/file/文件
        String parent =
                System.getProperty("user.dir") + "/static/file";
        // File对象指向这个路径，file是否存在
        File dir = new File(parent);
        if (!dir.exists()) { // 检测目录是否存在
            dir.mkdirs(); // 创建当前目录
        }
        // 获取到这个文件名称 uuid工具来将生成一个新的字符串作为文件名
        String originalFilename = file.getOriginalFilename();
        System.out.println("Originalfilename" + originalFilename);
        // 截取文件后缀
        String suffix = "";
        int index = originalFilename.lastIndexOf(".");
        suffix = originalFilename.substring(index);
        // 随机生成前缀名并拼接
        String filename = UUID.randomUUID().toString().toUpperCase() + suffix;

        File dest = new File(dir, filename); // 是一个空文件
        // 参数file中数据写入到这个空文件中
        try {
            file.transferTo(dest); //将file文件中的数据写入到dest文件中
        }
        catch (Exception e) {
            System.out.println("文件状态异常或文件读写异常");
        }

        // 返回文件的路径/upload/test.png
        String filePath = "/static/file/" + filename;
        return filePath;
    }

}
