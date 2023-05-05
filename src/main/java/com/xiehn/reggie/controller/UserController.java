package com.xiehn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiehn.reggie.common.R;
import com.xiehn.reggie.pojo.User;
import com.xiehn.reggie.service.UserService;
import com.xiehn.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 移动端手机发送短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if(phone!=null ){
            //生成随机的四位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("code= "+code);

            //调用阿里云的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉","SMS_460685320",phone,code);

            //将生成的验证码保存到session对象
            //session.setAttribute(phone,code);

            //将生成的验证码缓存到redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功！");
        }

        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     *
     * 这里之所以使用map集合接收数据是因为user类里面只有phone这个字段，没有code这个字段
     * 所以也可以采用定义一个UserDto类来实现
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();

        //从session当中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);

        //从redis中获取缓存的验证码
        Object codeInSession=redisTemplate.opsForValue().get(phone);

        //进行验证码的比对（页面提交的验证码和session当中的验证码）
        if(codeInSession!=null && codeInSession.equals(code)){
            //如果比对成功，说明登陆成功

            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);

            //判断当前手机号的用户是否为新用户，如果是新用户则自动完成注册
            if(user==null){
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            //如果用户登录成功，删除redis中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败！");
    }


}
