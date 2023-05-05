package com.xiehn.reggie.controller;

import com.xiehn.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件就会被删除
        log.info(file.toString());

        //原始文件名
        String originalFilename = file.getOriginalFilename();//adc.jpg
        String suffix=originalFilename.substring(originalFilename.lastIndexOf("."));//切割原始的文件名，获取文件名后缀 .jpg

        //使用UUID重新生成文件名，防止文件名重复造成文件覆盖
        String fileName=UUID.randomUUID().toString()+suffix;

        //创建一个目录对象
        File dir=new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置
            File file1=new File(basePath+fileName);
            file.transferTo(file1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //构造输入流,通过输入流读取文件内容
            FileInputStream inputStream=new FileInputStream(new File(basePath+name));

            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream=response.getOutputStream();
            response.setContentType("image/jpeg");

            int len=0;
            byte[] bytes=new byte[1024];

            //进行读取
            while ((len = inputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
