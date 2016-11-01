/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @fileoverview register.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.4.2.9, Oct 19, 2016
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
                url: Label.servePath + "/register",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        $("#registerTip").addClass('succ').removeClass('error').html('<ul><li>' + result.msg + '</li></ul>');

                        $("#registerBtn").attr('disabled', 'disabled');
                    } else {
                        $("#registerTip").removeClass("tip-succ");
                        $("#registerTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                        $("#captcha").attr("src", Label.servePath + "/captcha?code=" + Math.random());
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
                url: Label.servePath + "/register2",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        window.location.href = Label.servePath;
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
                url: Label.servePath + "/forget-pwd",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        $("#registerTip").addClass('succ').removeClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                    } else {
                        $("#registerTip").removeClass("tip-succ");
                        $("#registerTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                        $("#captcha").attr("src", Label.servePath + "/captcha?code=" + Math.random());
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
                url: Label.servePath + "/reset-pwd",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        window.location.href = Label.servePath;
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