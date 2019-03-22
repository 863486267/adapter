package com.saas.adapter.tools;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

@Slf4j
public class Analysis {


    public static String getAnalysis(String strurl){

        MultiFormatReader formatReader = new MultiFormatReader();
        URL url = null;
        try {
            url = new URL(strurl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Result result=null;
        try {
            BufferedImage image = ImageIO.read(url);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            HashMap hints = new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            result = formatReader.decode(binaryBitmap, hints);

            log.info("解析结果： " + result.toString());
            log.info("二维码的格式类型：" + result.getBarcodeFormat());
            log.info("二维码文本内容： " + result.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
