/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview register.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.4.1.8, Jul 3, 2016
 */

/**
 * @description Register
 * @static
 */
var Register = {
    /**
     * @description Register Step 1
     */
    register: function () {
        if (Validate.goValidate({target: $("#registerTip"),
            data: [{
                    "target": $("#userName"),
                    "msg": Label.userNameErrorLabel,
                    "type": 'string',
                    'max': 20
                }, {
                    "target": $("#userEmail"),
                    "msg": Label.invalidEmailLabel,
                    "type": "email"
                }, {
                    "target": $("#securityCode"),
                    "msg": Label.captchaErrorLabel,
                    "type": 'string',
                    'max': 4
                }]})) {
            var requestJSONObject = {
                userName: $("#userName").val().replace(/(^\s*)|(\s*$)/g, ""),
                userEmail: $("#userEmail").val().replace(/(^\s*)|(\s*$)/g, ""),
                invitecode: $("#invitecode").val().replace(/(^\s*)|(\s*$)/g, ""),
                captcha: $("#securityCode").val(),
                referral: $("#referral").val()
            };

            $.ajax({
                url: "/register",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        $("#registerTip").addClass('succ').removeClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                    } else {
                        $("#registerTip").removeClass("tip-succ");
                        $("#registerTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                        $("#captcha").attr("src", "/captcha?code=" + Math.random());
                        $("#securityCode").val("");
                    }
                }
            });
        }
    },
    /**
     * @description Register Step 2
     */
    register2: function () {
        if (Validate.goValidate({target: $("#registerTip"),
            data: [{
                    "target": $("#userPassword"),
                    "msg": Label.invalidPasswordLabel,
                    "type": 'password',
                    'max': 20
                }, {
                    "target": $("#confirmPassword"),
                    "original": $("#userPassword"),
                    "msg": Label.confirmPwdErrorLabel,
                    "type": "confirmPassword"
                }]})) {
            var requestJSONObject = {
                userAppRole: $("input[name=userAppRole]:checked").val(),
                userPassword: calcMD5($("#userPassword").val()),
                referral: $("#referral").val(),
                userId: $("#userId").val()
            };

            $.ajax({
                url: "/register2",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        window.location.href = "https://hacpai.com/article/1440573175609";
                    } else {
                        $("#registerTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                    }
                }
            });
        }
    },
    /**
     * @description Forget password
     */
    forgetPwd: function () {
        if (Validate.goValidate({target: $("#registerTip"),
            data: [{
                    "target": $("#userEmail"),
                    "msg": Label.invalidEmailLabel,
                    "type": "email"
                }, {
                    "target": $("#securityCode"),
                    "msg": Label.captchaErrorLabel,
                    "type": 'string',
                    'max': 4
                }]})) {
            var requestJSONObject = {
                userEmail: $("#userEmail").val().replace(/(^\s*)|(\s*$)/g, ""),
                captcha: $("#securityCode").val()
            };

            $.ajax({
                url: "/forget-pwd",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        $("#registerTip").addClass('succ').removeClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                    } else {
                        $("#registerTip").removeClass("tip-succ");
                        $("#registerTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                        $("#captcha").attr("src", "/captcha?code=" + Math.random());
                        $("#securityCode").val("");
                    }
                }
            });
        }
    },
    /**
     * @description Reset password
     */
    resetPwd: function () {
        if (Validate.goValidate({target: $("#registerTip"),
            data: [{
                    "target": $("#userPassword"),
                    "msg": Label.invalidPasswordLabel,
                    "type": 'password',
                    'max': 20
                }, {
                    "target": $("#confirmPassword"),
                    "original": $("#userPassword"),
                    "msg": Label.confirmPwdErrorLabel,
                    "type": "confirmPassword"
                }]})) {
            var requestJSONObject = {
                userPassword: calcMD5($("#userPassword").val()),
                userId: $("#userId").val()
            };

            $.ajax({
                url: "/reset-pwd",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        window.location.href = "https://hacpai.com";
                    } else {
                        $("#registerTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                    }
                }
            });
        }
    },
    init: function () {
        // 注册回车事件
        $("#securityCode").keyup(function (event) {
            if (event.keyCode === 13) {
                Register.register();
            }
        });

        // 表单错误状态
        $("input[type=text], input[type=password], textarea").blur(function () {
            $(this).removeClass("input-error");
        });

        $("#userName").focus();
    },
    init2: function () {
        // 注册回车事件
        $("#confirmPassword").keyup(function (event) {
            if (event.keyCode === 13) {
                Register.register();
            }
        });

        // 表单错误状态
        $("input[type=text], input[type=password], textarea").blur(function () {
            $(this).removeClass("input-error");
        });

        $("#userPassword").focus();
    }
};