package com.saas.adapter.code.controllers.FengGu;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.config.AlipayConfig;
import com.saas.adapter.entity.FengFuEntity;
import com.saas.adapter.po.MerchantChannelAptitude;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.MD5;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.zxing.client.j2se.MatrixToImageConfig.BLACK;
import static com.google.zxing.client.j2se.MatrixToImageConfig.WHITE;

@RestController
@RequestMapping("/fenggu")
@Slf4j
public class PayController {


    @Autowired
    RestTemplate template;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private TokenClient tokenClient;
    @Autowired
    public OrderReturnMain orderReturnMain;
    @Autowired
    public SaasNotifyParams saasNotifyParams;


    Map<String,FengFuEntity> mapdata=new ConcurrentHashMap<>();



    @PostMapping("/pay")
    public OrderReturn pay(@RequestBody Parameter parameter) throws Exception {
        if (parameter.aptitudes.isEmpty() || parameter.aptitudes.size() == 0) {
            return orderReturnMain.failReturn("未配置交易资质参数");
        }
        // 筛选资质
        MerchantChannelAptitude aptitude = selecAptitude(parameter.aptitudes, parameter.order.money, "ALIPAY");
        if (aptitude == null) {
            return orderReturnMain.failReturn("未找到可用资质参数");
        }
        if (StringUtils.isBlank(aptitude.account)) {
            return orderReturnMain.failReturn("转账账号不能为空");
        }
        if (StringUtils.isBlank(aptitude.domain)) {
            return orderReturnMain.failReturn("支付域名地址不能为空");
        }

        log.info(String.valueOf(JSONObject.parse(JSON.toJSONString(aptitude))));
        String account=aptitude.account;
        String uid=aptitude.url;
        String mark=parameter.order.no;
        String money= MD5.changeF2Y(parameter.order.money);
        String nomber=aptitude.name;
        nomber=nomber.substring(nomber.indexOf("-")+1);
        log.info("终端手机："+nomber);
        log.info("登陆账号："+account);
        log.info("uid："+uid);
        log.info("money："+money);

        tokenClient.savePayUrl(mark, parameter.order.orderConfig.notifyUrl, 50);
        //https://ds.alipay.com/?from=mobilecodec&scheme=alipayqr%3A%2F%2Fplatformapi%2Fstartapp%3FsaId%3D10000007%26qrcode%3D
        String url="alipays://platformapi/startapp?appId=20000691%26url=http://47.106.184.173:8983/bill2.html?str="+account+"Z"+uid+"Z"+mark+"Z"+money+"Z"+nomber;
        log.info(url);
        return orderReturnMain.successReturn(url, aptitude.name, parameter.order.money);
    }
    @PostMapping("/payH5")
    public OrderReturn payH5(@RequestBody Parameter parameter) throws Exception {
        if (parameter.aptitudes.isEmpty() || parameter.aptitudes.size() == 0) {
            return orderReturnMain.failReturn("未配置交易资质参数");
        }
        // 筛选资质
        MerchantChannelAptitude aptitude = selecAptitude(parameter.aptitudes, parameter.order.money, "ALIPAY");
        if (aptitude == null) {
            return orderReturnMain.failReturn("未找到可用资质参数");
        }
        if (StringUtils.isBlank(aptitude.account)) {
            return orderReturnMain.failReturn("转账账号不能为空");
        }
        if (StringUtils.isBlank(aptitude.domain)) {
            return orderReturnMain.failReturn("支付域名地址不能为空");
        }

        log.info(String.valueOf(JSONObject.parse(JSON.toJSONString(aptitude))));
        String account=aptitude.account;
        String uid=aptitude.url;
        String mark=parameter.order.no;
        String money= MD5.changeF2Y(parameter.order.money);
        String nomber=aptitude.name;
        nomber=nomber.substring(nomber.indexOf("-")+1);
        log.info("终端手机："+nomber);
        log.info("登陆账号："+account);
        log.info("uid："+uid);

        tokenClient.savePayUrl(mark, parameter.order.orderConfig.notifyUrl, 50);
        //https://ds.alipay.com/?from=mobilecodec&scheme=alipayqr%3A%2F%2Fplatformapi%2Fstartapp%3FsaId%3D10000007%26qrcode%3D
//        String url="alipays://platformapi/startapp?appId=20000691%26url=http://47.106.184.173:8983/bill2.html?str="+account+"Z"+uid+"Z"+mark+"Z"+money+"Z"+nomber;
        String url="http://47.106.220.74:8981/fenggu/receive?str="+account+"Z"+uid+"Z"+mark+"Z"+money+"Z"+nomber;
        return orderReturnMain.successReturn(url, aptitude.name, parameter.order.money);
    }


    private MerchantChannelAptitude selecAptitude(List<MerchantChannelAptitude> merchantChannelAptitude, String money,
                                                  String payType) {

        List<MerchantChannelAptitude> screening = new ArrayList<MerchantChannelAptitude>();

        for (MerchantChannelAptitude MerchantChannelAptitude : merchantChannelAptitude) {
            if (!MerchantChannelAptitude.enabled) {
                // 排除未激活的资质
                continue;
            }
            if (!payType.equals(MerchantChannelAptitude.type.toString())) {
                // 排除不符合交易类型的资质
                continue;
            }
            if (MerchantChannelAptitude.dayMax == 0) {
                // 未设置每日交易金额的资质直接添加
                screening.add(MerchantChannelAptitude);
                continue;
            }
            Long moneying = tokenClient.getPayMongIng(MerchantChannelAptitude.aptitudeId);
            if (MerchantChannelAptitude.dayMax - moneying - Long.valueOf(money) < 0) {
                // 排除超出日限交易的资质
                log.info(
                        "资质名称:" + MerchantChannelAptitude.name + ",资质账号:" + MerchantChannelAptitude.account + " 额度已上限");
                continue;
            }
            screening.add(MerchantChannelAptitude);
        }
        if (screening == null || screening.size() == 0) {
            log.info("没有符合条件的资质");
            return null;
        }
        Gson gson = new Gson();
        int index = (int) (Math.random() * screening.size());
        try {
            // 从符合条件的集合中随机挑选一组资质
            MerchantChannelAptitude arbitration = gson.fromJson(gson.toJson(screening.get(index)),
                    MerchantChannelAptitude.class);
            return arbitration;
        } catch (JsonSyntaxException e) {
            log.info("JSON格式转化错误--->" + e.getMessage());
            return null;
        }
    }

    public static String getMark(){
        Date date=new Date();
        long times=date.getTime();
        DateFormat format=new SimpleDateFormat("MM-dd HH:mm:ss");
        String time=format.format(times);
        time=time.replaceAll("-","");
        time=time.replaceAll(" ","");
        time=time.replaceAll(":","");
        int i=(int)(Math.random()*9999);
        time=time+i;
        return time;
    }


}
