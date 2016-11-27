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
 * @version 2.5.2.10, Nov 26, 2016
 */

/**
 * @description Verify
 * @static
 */
var Verify = {  
    /**
     * @description 登录
     */
    login: function (goto) {
        if (Validate.goValidate({target: $('#loginTip'),
            data: [{
                    "target": $("#nameOrEmail"),
                    "type": "string",
                    "max": 256,
                    "msg": Label.loginNameErrorLabel
                }, {
                    "target": $("#loginPassword"),
                    "type": "password",
                    "msg": Label.invalidPasswordLabel
                }]})) {
            var requestJSONObject = {
                nameOrEmail: $("#nameOrEmail").val().replace(/(^\s*)|(\s*$)/g, ""),
                userPassword: calcMD5($("#loginPassword").val()),
                rememberLogin: $("#rememberLogin").prop("checked"),
                captcha: $('#captchaLogin').val().replace(/(^\s*)|(\s*$)/g, "")
            };

            $.ajax({
                url: Label.servePath + "/login",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        window.location.href = goto;
                    } else {
                        $("#loginTip").addClass('error').html('<ul><li>' + result.msg + '</li></ul>');

                        if (result.needCaptcha && "" !== result.needCaptcha) {
                            $('#captchaImg').parent().show();
                            $("#captchaImg").attr("src", Label.servePath + "/captcha/login?needCaptcha="
                                    + result.needCaptcha + "&t=" + Math.random())
                                    .click(function () {
                                        $(this).attr('src', Label.servePath + "/captcha/login?needCaptcha="
                                                + result.needCaptcha + "&t=" + Math.random())
                                    });
                        }
                    }
                }
            });
        }
    },
    /**
     * @description Register Step 1
     */
    register: function () {
        if (Validate.goValidate({target: $("#registerTip"),
            data: [{
                    "target": $("#registerUserName"),
                    "msg": Label.userNameErrorLabel,
                    "type": 'string',
                    'max': 20
                }, {
                    "target": $("#registerUserEmail"),
                    "msg": Label.invalidEmailLabel,
                    "type": "email"
                }]})) {
            var requestJSONObject = {
                userName: $("#registerUserName").val().replace(/(^\s*)|(\s*$)/g, ""),
                userEmail: $("#registerUserEmail").val().replace(/(^\s*)|(\s*$)/g, ""),
                invitecode: $("#registerInviteCode").val().replace(/(^\s*)|(\s*$)/g, ""),
                captcha: $("#registerCaptcha").val(),
                referral: $("#referral").val()
            };

            $("#registerBtn").attr('disabled', 'disabled');

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
                        $("#registerTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                        $("#registerCaptchaImg").attr("src", Label.servePath + "/captcha?code=" + Math.random());
                        $("#registerCaptcha").val("");
                        $("#registerBtn").removeAttr('disabled');
                    }
                }
            });
        }
    },
    /**
     * @description Register Step 2
     */
    register2: function () {
        if (Validate.goValidate({target: $("#registerTip2"),
            data: [{
                    "target": $("#registerUserPassword2"),
                    "msg": Label.invalidPasswordLabel,
                    "type": 'password',
                    'max': 20
                }, {
                    "target": $("#registerConfirmPassword2"),
                    "original": $("#registerUserPassword2"),
                    "msg": Label.confirmPwdErrorLabel,
                    "type": "confirmPassword"
                }]})) {
            var requestJSONObject = {
                userAppRole: $("input[name=userAppRole]:checked").val(),
                userPassword: calcMD5($("#registerUserPassword2").val()),
                referral: $("#referral2").val(),
                userId: $("#userId2").val()
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
                        $("#registerTip2").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                    }
                }
            });
        }
    },
    /**
     * @description Forget password
     */
    forgetPwd: function () {
        if (Validate.goValidate({target: $("#fpwdTip"),
            data: [{
                    "target": $("#fpwdEmail"),
                    "msg": Label.invalidEmailLabel,
                    "type": "email"
                }, {
                    "target": $("#fpwdSecurityCode"),
                    "msg": Label.captchaErrorLabel,
                    "type": 'string',
                    'max': 4
                }]})) {
            var requestJSONObject = {
                userEmail: $("#fpwdEmail").val().replace(/(^\s*)|(\s*$)/g, ""),
                captcha: $("#fpwdSecurityCode").val()
            };

            $.ajax({
                url: Label.servePath + "/forget-pwd",
                type: "POST",
                cache: false,
                data: JSON.stringify(requestJSONObject),
                success: function (result, textStatus) {
                    if (result.sc) {
                        $("#fpwdTip").addClass('succ').removeClass('error').html('<ul><li>' + result.msg + '</li></ul>');
                    } else {
                        $("#fpwdTip").removeClass("tip-succ");
                        $("#fpwdTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                        $("#fpwdCaptcha").attr("src", Label.servePath + "/captcha?code=" + Math.random());
                        $("#fpwdSecurityCode").val("");
                    }
                }
            });
        }
    },
    /**
     * @description Reset password
     */
    resetPwd: function () {
        if (Validate.goValidate({target: $("#rpwdTip"),
            data: [{
                    "target": $("#rpwdUserPassword"),
                    "msg": Label.invalidPasswordLabel,
                    "type": 'password',
                    'max': 20
                }, {
                    "target": $("#rpwdConfirmPassword"),
                    "original": $("#rpwdUserPassword"),
                    "msg": Label.confirmPwdErrorLabel,
                    "type": "confirmPassword"
                }]})) {
            var requestJSONObject = {
                userPassword: calcMD5($("#rpwdUserPassword").val()),
                userId: $("#rpwdUserId").val()
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
                        $("#rpwdTip").addClass('error').removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
                    }
                }
            });
        }
    },
    init: function () {
        // 注册回车事件
        $("#registerCaptcha, #registerInviteCode").keyup(function (event) {
            if (event.keyCode === 13) {
                Verify.register();
            }
        });

        // 忘记密码回车事件
        $("#fpwdSecurityCode").keyup(function (event) {
            if (event.keyCode === 13) {
                Verify.forgetPwd();
            }
        });

        // 登录密码输入框回车事件
        $("#loginPassword, #captchaLogin").keyup(function (event) {
            if (event.keyCode === 13) {
                $('#loginTip').next().click();
            }
        });

        // 重置密码输入框回车事件
        $("#rpwdConfirmPassword").keyup(function (event) {
            if (event.keyCode === 13) {
                Verify.resetPwd();
            }
        });
    }
};