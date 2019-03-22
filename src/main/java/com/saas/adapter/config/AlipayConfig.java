package com.saas.adapter.config;

import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AlipayConfig {

        /**
         * 支付宝网关（固定）
         */
        public static final  String URL="https://openapi.alipay.com/gateway.do";

        /**
         * 授权url
         */
        public static final  String ALIPAY_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm";

        /**
         * 应用id
         */
        public static final  String APP_ID="2018020802163344";

        /**
         * 应用私钥
         */
          public static final  String APP_PRIVATE_KEY="MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCFs5UvM8Fmx/hPE2xYWcgKOQ1/D/S2AWxsTeUMWpKOfj4qFKDLw67kKRAw+M8Ib7+BpHjrUoUEkPAgNc9J1RL4l5XjNUeURGt4FSTlgC/rHlrAyQhoiFh7/VDqf9sn0mwBBGIRNzPgacKFYQrcLotbTacI5tEmZsB98k+PFJyBfawwhDEDZ8Hy8CXq3gyN0LyYGWQVL37JVpi8JGnOAmJSSqCaF/Tx/oeMYqdjRkrysT4wjbH4NHriK7OQxh2Icd+cagywdtk/UcHmnU9tiW7FDhqKi4+MMATli2ZcEeRR1Rm7NlsbHGgcC9opAiFjKgnm4nTWtKL5V6ECuQFq+RtHAgMBAAECggEARKDxqtD+LSvoIW7u5krvi4/NhbNDAUmpxc4WDElRgSL79c2w2+cOFr+dOAX+/22c94bdKq2o6bccCuyUAmoE2uW0YnQu0cRtBa6UydkKv71NCDgowL976+kTZ8tJSx+PoNYHIJ8dWcvvtVcio/KgRbWOT9tq8DM+C2gdvmvuCekYT3RlZqIUNcLoNaqfQZzxE6nxH2V4aI9mAMS2mtquPVKZYbzvOScmff+7b35IVqmYS/gtPBoAq1LhbJVmmBpC9vjRSHCihYpINO1RHOuHeIgVaCEEHWO8yzW7jWzf+mlLWwjLmbkmdnbWGe7hVW9rTpqzLkN++Dm3AVs2YJCuwQKBgQD/oiEL6U23tsnPxkWiXnRCkxtsU9t8B3D2YZBFhyP19vCr5wqxStyaxRZjwoRHusOD+l+ay735hNTmtoQycMTT6wHAEKij+qS7+UElWt/CiDTcxfaPl4+HRaFew1DiPCiemL3sbwzwO3WYtUwS0woRMUoEb17FL5xVqq4QExYfvQKBgQCF5K3eViIdiPymMhbTK60u1AWKlluPtZnJ7Kt2po/w1Nx+AvBmXnRMUh4bNagzZdE+Vu4JI4T1XHWw6atG+rDMANOwhMKeTMwWzAREyDtyXvHaOkZfH3zMaUkiLVvI3OyLLIt/hEUKijgKKszNFZFimDSksTFcBk1KSRw8EzilUwKBgEvy0FyomRV+8iCP6Lpf06y/5gLeOzRzRYV94Q43I2D9V+3Ku+uGsVouOgOzk/d3roT0ixkDdVK/uz08drRBusi3ZpK4oFHvsqfCWy+M+zPhocsB6copnlPzRQlRgCNm+i58dNjc+XwaPkr7ejmRh8kp2rLMn9x307vD9OkDsqwFAoGAU2bx4vf2Qj2NWx0CWMHxG6VYWk50dF0jkcNJvxvbt6FwQ1IjWzDk8pGITVysDHAU2eceWTT14jyY1v2HAiywKjMtqdvYuvCNhHnLAulTFlgMAOqh4Sjk/RNsSczFSqZK9hpmPqUuuHUGmVa1wd34dNjomdoCLBeE14wY+Vbjxx8CgYAyIkgXZlykFO/FmpcufrXpOLWMRfquXpmdtXkH0pb6aR5CWfxGxCRyXez5p3wjeQiMbOBg5JVKXbgSzn5ct3bZXVsLdamLcDG7OVSG/Qo+kZEb6PQBM7xW8alAczvPnmb0B5GKt4iyd75S1DMSXZCwdKkUm0Y6Wri+dGJ6a5AMdw==";

        /**
         * 参数返回格式
         */
        public static final  String FORMAT="json";

        /**
         * 编码集
         */
        public static final  String CHARSET="UTF-8";

        /**
         * 支付宝公钥
         */
        public static final  String ALIPAY_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo5E+4MC1LkwRyS+bmR2SE8afdFJXPzNeX+IuDg/ka6xIw8X/mCWfPaoQEQYfYey8Q/EjoWGQMQfdn6DS3eiLRV4zQC+O6HmPVaZS1wtZPK9KRovg7MH0RS986wP3Acs3vkNSB95Hh4ZlMI3qlwZ+xA0lSZLchCzEtzsXyTT12Pem8JaVTCC1PpsksR7dLOxSAGApKtHPjjg1MI17w4xVTv5Hm/tPaNWrzShQho38WTAFaLHLfS0JL0w8XupGMkB37bt8+GjnqzKzekFip/HdjcYDFTF44U1CYPNy70WxsaycUaygLiOxnVc008bcYff4ZjBYD5i0hHxc3UIlv9o91wIDAQAB";

        /**
         * 商户生成签名字符串所使用的签名算法类型
         */
        public static final  String SIGN_TYPE="RSA2";


        /**
         *  跳转到授权界面
         */


    /**
     * 获取用户信息
     * @param request
     * @param response
     */


    }
