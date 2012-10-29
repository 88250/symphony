/*
 * Copyright (c) 2012, B3log Team
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
 * @fileoverview settings.
 *
 * @author <a href="mailto:LLY219@gmail.com">Liyuan Li</a>
 * @version 1.0.0.2, Oct 29, 2012
 */
/**
 * @description Settings function
 * @static
 */
var Settings = {
    /**
     * @description 更新 settings 页面数据。
     */
    update: function (type) {
        var requestJSONObject = {};
        switch (type) {
            case "profiles":
                requestJSONObject = this._validateProfiles();
                break;
            case "sync/b3":
                requestJSONObject = this._validateSyncB3();
                break;
            case "password":
                requestJSONObject = this._validatePassword();
                break;
            default:
                console.log("update settings has no type");
        }
        
        if (!requestJSONObject) {
            return;
        }
        
        $.ajax({
            url: "/settings/" + type,
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                $("#" + type.replace(/\//g, "") + "Tip").removeClass("tip-succ").removeClass("tip-error").text("");
            },
            success: function(result, textStatus){
                if (result.sc) {
                    $("#" + type.replace(/\//g, "") + "Tip").addClass("tip-succ").text(Label.updateSuccLabel).css({
                        "border-left": "1px solid #78909B",
                        "width": "860px"
                    });
                } else {
                    $("#" + type.replace(/\//g, "") + "Tip").addClass("tip-error").text(result.msg).css({
                        "border-left": "1px solid #E2A0A0",
                        "width": "855px"
                    });
                }
            }
        });
    },
    
    /**
     * @description settings 页面 profiles 数据校验
     * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
     */
    _validateProfiles: function () {
        var URLVal = $("#userURL").val().replace(/(^\s*)|(\s*$)/g,""),
        QQVal = $("#userQQ").val().replace(/(^\s*)|(\s*$)/g,""),
        introVal = $("#userIntro").val().replace(/(^\s*)|(\s*$)/g,"");
        if (Validate.goValidate([{
            "id": "userURL",
            "type": "url",
            "msg": Label.invalidUserURLLabel
        }, {
            "id": "userQQ",
            "type": "12",
            "msg": Label.invalidUserQQLabel
        }, {
            "id": "userIntro",
            "type": "255",
            "msg": Label.invalidUserIntroLabel
        }])) {
            var data = {};
            data.userURL = URLVal;
            data.userQQ = QQVal;
            data.userIntro = introVal;
            return data;
        } 
        return false;
    },
    
    /**
     * @description settings 页面 solo 数据同步校验
     * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
     */
    _validateSyncB3: function () {
        var keyVal = $("#soloKey").val().replace(/(^\s*)|(\s*$)/g,""),
        postURLVal = $("#soloPostURL").val().replace(/(^\s*)|(\s*$)/g,""),
        cmtURLVal = $("#soloCmtURL").val().replace(/(^\s*)|(\s*$)/g,"");
        
        if (Validate.goValidate([{
            "id": "soloKey",
            "type": "20",
            "msg": Label.invalidUserB3KeyLabel
        }, {
            "id": "soloPostURL",
            "type": "150",
            "msg": Label.invalidUserB3ClientURLLabel
        }, {
            "id": "soloCmtURL",
            "type": "150",
            "msg": Label.invalidUserB3ClientURLLabel
        }])) {
            var data = {};
            data.userB3Key = keyVal;
            data.userB3ClientAddArticleURL = postURLVal;
            data.userB3ClientAddCommentURL = cmtURLVal;
            return data;
        }
        return false;
    },
    
    /**
 * @description settings 页面密码校验
 * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
 */
    _validatePassword: function () {
        var pwdVal = $("#pwdOld").val(),
        newPwdVal = $("#pwdNew").val();
        if (Validate.goValidate([{
            "id": "pwdOld",
            "type": "password",
            "msg": Label.invalidPasswordLabel
        }, {
            "id": "pwdNew",
            "type": "password",
            "msg": Label.invalidPasswordLabel
        }, {
            "id": "pwdRepeat",
            "type": "confirmPassword|pwdNew",
            "msg": Label.confirmPwdErrorLabel
        }])) {
            if (newPwdVal !== $("#pwdRepeat").val()) {
                return false;
            }
            var data = {};
            data.userPassword = pwdVal;
            data.userNewPassword = newPwdVal;
            return data;
        }
        return false;
    }
};

