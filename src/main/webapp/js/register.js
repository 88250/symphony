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
 * @fileoverview register.
 *
 * @author <a href="mailto:LLY219@gmail.com">Liyuan Li</a>
 * @version 1.0.0.1, Aug 7, 2012
 */

/**
 * @description Register
 * @static
 */
var Register = {
    /**
     * @description 前端校验
     */
    _validate: function () {
        var $userName = $("#userName"),
        $userEmail = $("#userEmail"),
        $userPassword = $("#userPassword"),
        $confirmPassword = $("#confirmPassword"),
        $securityCode = $("#securityCode"),
        $tip = $("#tip");
        
        var nameVal = $userName.val().replace(/(^\s*)|(\s*$)/g,""),
        emailVal = $userEmail.val().replace(/(^\s*)|(\s*$)/g,"");
        
        if (nameVal.length === 0 || nameVal.length > 21 ) {
            $tip.text("用户名长度为1~20");
        } else if (!/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test(emailVal)) {
            $tip.text("邮件格式不正确");
        } else if (emailVal.length > 257) {
            $tip.text("邮件长度为1~257");
        } else if ($userPassword.val().length === 0 || $userPassword.val().length > 17) {
            $tip.text("密码长度为1~16");
        } else if ($confirmPassword.val() !== $userPassword.val()) {
            $tip.text("密码输入不一致");
        } else if ($securityCode.val().replace(/(^\s*)|(\s*$)/g,"").length === 0) {
            $tip.text("验证码不能为空");
        } else {
            return true;
        }
        return false;
    },
    
    /**
     * @description 注册
     */
    register: function () {
        if (this._validate()) {
            
        }
    }
};