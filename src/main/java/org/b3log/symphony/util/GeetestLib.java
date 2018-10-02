/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <a href="https://github.com/GeeTeam/gt-java-sdk">Geetest Java SDK</a>.
 *
 * @author unascribed
 * @since 1.4.0
 */
public class GeetestLib {

    protected final String verName = "3.2.0";// SDK版本编号
    protected final String sdkLang = "java";// SD的语言类型

    protected final String apiUrl = "http://api.geetest.com"; //极验验证API URL
    protected final String baseUrl = "api.geetest.com";

    protected final String registerUrl = "/register.php"; //register url
    protected final String validateUrl = "/validate.php"; //validate url

    /**
     * 极验验证二次验证表单数据 chllenge
     */
    public static final String fn_geetest_challenge = "geetest_challenge";

    /**
     * 极验验证二次验证表单数据 validate
     */
    public static final String fn_geetest_validate = "geetest_validate";

    /**
     * 极验验证二次验证表单数据 seccode
     */
    public static final String fn_geetest_seccode = "geetest_seccode";

    /**
     * 公钥
     */
    private String captchaId = "";

    /**
     * 私钥
     */
    private String privateKey = "";

    private String userId = "";

    private String responseStr = "";

    /**
     * 调试开关，是否输出调试日志
     */
    public boolean debugCode = false;

    /**
     * 极验验证API服务状态Session Key
     */
    public static final String gtServerStatusSessionKey = "gt_server_status";

    /**
     * 带参数构造函数
     *
     * @param captchaId
     * @param privateKey
     */
    public GeetestLib(String captchaId, String privateKey) {
        this.captchaId = captchaId;
        this.privateKey = privateKey;
    }

    /**
     * 获取本次验证初始化返回字符串
     *
     * @return 初始化结果
     */
    public String getResponseStr() {
        return responseStr;
    }

    public String getVersionInfo() {
        return verName;
    }

    /**
     * 预处理失败后的返回格式串
     *
     * @return
     */
    private String getFailPreProcessRes() {

        Long rnd1 = Math.round(Math.random() * 100);
        Long rnd2 = Math.round(Math.random() * 100);
        String md5Str1 = md5Encode(rnd1 + "");
        String md5Str2 = md5Encode(rnd2 + "");
        String challenge = md5Str1 + md5Str2.substring(0, 2);

        return String.format(
                "{\"success\":%s,\"gt\":\"%s\",\"challenge\":\"%s\"}", 0,
                this.captchaId, challenge);
    }

    /**
     * 预处理成功后的标准串
     */
    private String getSuccessPreProcessRes(String challenge) {

        gtlog("challenge:" + challenge);
        return String.format(
                "{\"success\":%s,\"gt\":\"%s\",\"challenge\":\"%s\"}", 1,
                this.captchaId, challenge);
    }

    /**
     * 验证初始化预处理
     *
     * @return 1表示初始化成功，0表示初始化失败
     */
    public int preProcess() {

        if (registerChallenge() != 1) {

            this.responseStr = this.getFailPreProcessRes();
            return 0;
        }

        return 1;

    }

    /**
     * 验证初始化预处理
     *
     * @param userid
     * @return 1表示初始化成功，0表示初始化失败
     */
    public int preProcess(String userid) {

        this.userId = userid;
        return this.preProcess();
    }

    /**
     * 用captchaID进行注册，更新challenge
     *
     * @return 1表示注册成功，0表示注册失败
     */
    private int registerChallenge() {
        try {
            String GET_URL = apiUrl + registerUrl + "?gt=" + this.captchaId;
            if (this.userId != "") {
                GET_URL = GET_URL + "&user_id=" + this.userId;
                this.userId = "";
            }
            gtlog("GET_URL:" + GET_URL);
            String result_str = readContentFromGet(GET_URL);
            gtlog("register_result:" + result_str);
            if (32 == result_str.length()) {

                this.responseStr = this.getSuccessPreProcessRes(this.md5Encode(result_str + this.privateKey));

                return 1;
            } else {
                gtlog("gtServer register challenge failed");
                return 0;
            }
        } catch (Exception e) {
            gtlog("exception:register api");
        }
        return 0;
    }

    /**
     * 发送请求，获取服务器返回结果
     *
     * @param getURL
     * @return 服务器返回结果
     * @throws IOException
     */
    private String readContentFromGet(String getURL) throws IOException {

        URL getUrl = new URL(getURL);
        HttpURLConnection connection = (HttpURLConnection) getUrl
                .openConnection();

        connection.setConnectTimeout(2000);// 设置连接主机超时（单位：毫秒）
        connection.setReadTimeout(2000);// 设置从主机读取数据超时（单位：毫秒）

        // 建立与服务器的连接，并未发送数据
        connection.connect();

        // 发送数据到服务器并使用Reader读取返回的数据
        StringBuffer sBuffer = new StringBuffer();

        InputStream inStream = null;
        byte[] buf = new byte[1024];
        inStream = connection.getInputStream();
        for (int n; (n = inStream.read(buf)) != -1; ) {
            sBuffer.append(new String(buf, 0, n, StandardCharsets.UTF_8));
        }
        inStream.close();
        connection.disconnect();// 断开连接

        return sBuffer.toString();
    }

    /**
     * 判断一个表单对象值是否为空
     *
     * @param gtObj
     * @return
     */
    protected boolean objIsEmpty(Object gtObj) {
        if (gtObj == null) {
            return true;
        }

        return gtObj.toString().trim().length() == 0;

    }

    /**
     * 检查客户端的请求是否合法,三个只要有一个为空，则判断不合法
     *
     * @param request
     * @return
     */
    private boolean resquestIsLegal(String challenge, String validate, String seccode) {

        if (objIsEmpty(challenge)) {
            return false;
        }

        if (objIsEmpty(validate)) {
            return false;
        }

        return !objIsEmpty(seccode);
    }

    /**
     * 服务正常的情况下使用的验证方式,向gt-server进行二次验证,获取验证结果
     *
     * @param challenge
     * @param validate
     * @param seccode
     * @return 验证结果, 1表示验证成功0表示验证失败
     */
    public int enhencedValidateRequest(String challenge, String validate, String seccode) {

        if (!resquestIsLegal(challenge, validate, seccode)) {
            return 0;
        }
        gtlog("request legitimate");

        String host = baseUrl;
        String path = validateUrl;
        int port = 80;
        String query = String.format("seccode=%s&sdk=%s", seccode,
                (this.sdkLang + "_" + this.verName));
        String response = "";

        if (this.userId != "") {
            query = query + "&user_id=" + this.userId;
            this.userId = "";
        }
        gtlog(query);
        try {
            if (validate.length() <= 0) {
                return 0;
            }

            if (!checkResultByPrivate(challenge, validate)) {
                return 0;
            }
            gtlog("checkResultByPrivate");
            response = postValidate(host, path, query, port);

            gtlog("response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gtlog("md5: " + md5Encode(seccode));

        if (response.equals(md5Encode(seccode))) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 服务正常的情况下使用的验证方式,向gt-server进行二次验证,获取验证结果
     *
     * @param challenge
     * @param validate
     * @param seccode
     * @param userid
     * @return 验证结果, 1表示验证成功0表示验证失败
     */
    public int enhencedValidateRequest(String challenge, String validate, String seccode, String userid) {

        this.userId = userid;
        return this.enhencedValidateRequest(challenge, validate, seccode);
    }

    /**
     * failback使用的验证方式
     *
     * @param challenge
     * @param validate
     * @param seccode
     * @return 验证结果, 1表示验证成功0表示验证失败
     */
    public int failbackValidateRequest(String challenge, String validate, String seccode) {

        gtlog("in failback validate");

        if (!resquestIsLegal(challenge, validate, seccode)) {
            return 0;
        }
        gtlog("request legitimate");

        String[] validateStr = validate.split("_");
        String encodeAns = validateStr[0];
        String encodeFullBgImgIndex = validateStr[1];
        String encodeImgGrpIndex = validateStr[2];

        gtlog(String.format(
                "encode----challenge:%s--ans:%s,bg_idx:%s,grp_idx:%s",
                challenge, encodeAns, encodeFullBgImgIndex, encodeImgGrpIndex));

        int decodeAns = decodeResponse(challenge, encodeAns);
        int decodeFullBgImgIndex = decodeResponse(challenge, encodeFullBgImgIndex);
        int decodeImgGrpIndex = decodeResponse(challenge, encodeImgGrpIndex);

        gtlog(String.format("decode----ans:%s,bg_idx:%s,grp_idx:%s", decodeAns,
                decodeFullBgImgIndex, decodeImgGrpIndex));

        int validateResult = validateFailImage(decodeAns, decodeFullBgImgIndex, decodeImgGrpIndex);

        return validateResult;
    }

    /**
     * @param ans
     * @param full_bg_index
     * @param img_grp_index
     * @return
     */
    private int validateFailImage(int ans, int full_bg_index,
                                  int img_grp_index) {
        final int thread = 3;// 容差值

        String full_bg_name = md5Encode(full_bg_index + "").substring(0, 9);
        String bg_name = md5Encode(img_grp_index + "").substring(10, 19);

        String answer_decode = "";

        // 通过两个字符串奇数和偶数位拼接产生答案位
        for (int i = 0; i < 9; i++) {
            if (i % 2 == 0) {
                answer_decode += full_bg_name.charAt(i);
            } else if (i % 2 == 1) {
                answer_decode += bg_name.charAt(i);
            } else {
                gtlog("exception");
            }
        }

        String x_decode = answer_decode.substring(4);

        int x_int = Integer.valueOf(x_decode, 16);// 16 to 10

        int result = x_int % 200;
        if (result < 40) {
            result = 40;
        }

        if (Math.abs(ans - result) <= thread) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 解码随机参数
     *
     * @param encodeStr
     * @param challenge
     * @return
     */
    private int decodeResponse(String challenge, String string) {
        if (string.length() > 100) {
            return 0;
        }

        int[] shuzi = new int[]{1, 2, 5, 10, 50};
        String chongfu = "";
        HashMap<String, Integer> key = new HashMap<String, Integer>();
        int count = 0;

        for (int i = 0; i < challenge.length(); i++) {
            String item = challenge.charAt(i) + "";

            if (chongfu.contains(item) == true) {
                continue;
            } else {
                int value = shuzi[count % 5];
                chongfu += item;
                count++;
                key.put(item, value);
            }
        }

        int res = 0;

        for (int j = 0; j < string.length(); j++) {
            res += key.get(string.charAt(j) + "");
        }

        res = res - decodeRandBase(challenge);

        return res;

    }

    /**
     * 输入的两位的随机数字,解码出偏移量
     *
     * @param randStr
     * @return
     */
    private int decodeRandBase(String challenge) {

        String base = challenge.substring(32, 34);
        ArrayList<Integer> tempArray = new ArrayList<Integer>();

        for (int i = 0; i < base.length(); i++) {
            char tempChar = base.charAt(i);
            Integer tempAscii = (int) (tempChar);

            Integer result = (tempAscii > 57) ? (tempAscii - 87)
                    : (tempAscii - 48);

            tempArray.add(result);
        }

        int decodeRes = tempArray.get(0) * 36 + tempArray.get(1);
        return decodeRes;

    }

    /**
     * 输出debug信息，需要开启debugCode
     *
     * @param message
     */
    public void gtlog(String message) {
        if (debugCode) {
            System.out.println("gtlog: " + message);
        }
    }

    protected boolean checkResultByPrivate(String challenge, String validate) {
        String encodeStr = md5Encode(privateKey + "geetest" + challenge);
        return validate.equals(encodeStr);
    }

    /**
     * 貌似不是Post方式，后面重构时修改名字
     *
     * @param host
     * @param path
     * @param data
     * @param port
     * @return
     * @throws Exception
     */
    protected String postValidate(String host, String path, String data,
                                  int port) throws Exception {
        String response = "error";

        InetAddress addr = InetAddress.getByName(host);
        Socket socket = new Socket(addr, port);
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(), StandardCharsets.UTF_8));
        wr.write("POST " + path + " HTTP/1.0\r\n");
        wr.write("Host: " + host + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("Content-Length: " + data.length() + "\r\n");
        wr.write("\r\n"); // 以空行作为分割

        // 发送数据
        wr.write(data);
        wr.flush();

        // 读取返回信息
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = rd.readLine()) != null) {
            response = line;
        }
        wr.close();
        rd.close();
        socket.close();
        return response;
    }

    /**
     * md5 加密
     *
     * @param plainText
     * @return
     * @time 2014年7月10日 下午3:30:01
     */
    private String md5Encode(String plainText) {
        String re_md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer();
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5;
    }

}
