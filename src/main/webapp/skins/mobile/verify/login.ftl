<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${loginLabel} - ${symphonyLabel}">
        <meta name="description" content="${registerLabel} ${symphonyLabel}"/>
        </@head>
        <link rel="canonical" href="${servePath}/register">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper verify">
                <div class="verify-wrap">
                    <div class="form">
                        <svg><use xlink:href="#logo"></use></svg>
                        <div class="input-wrap">
                            <svg><use xlink:href="#userrole"></use></svg>
                            <input id="nameOrEmail" type="text" autofocus="autofocus" placeholder="${nameOrEmailLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <svg><use xlink:href="#locked"></use></svg>
                            <input type="password" id="loginPassword" placeholder="${passwordLabel}" />
                        </div>
                        <div class="fn-none input-wrap">
                            <img id="captchaImg" class="captcha-img fn-pointer" />
                            <input type="text" id="captchaLogin" class="captcha-input" placeholder="${captchaLabel}" />
                        </div>

                        <div class="fn-clear">
                            <div class="fn-hr5"></div>
                            <input type="checkbox" id="rememberLogin" checked /> ${rememberLoginStatusLabel}
                            <a href="${servePath}/forget-pwd" class="fn-right">${forgetPwdLabel}</a>
                            <div class="fn-hr5"></div>
                        </div>
                        
                        <div id="loginTip" class="tip"></div>
                        <button class="green" onclick="Verify.login('${goto}')">${loginLabel}</button>
                        <button onclick="Util.goRegister()">${registerLabel}</button>
                    </div>
                </div>
                <div class="intro content-reset">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/verify${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Verify.init();
            Label.invalidUserNameLabel = "${invalidUserNameLabel}";
            Label.invalidEmailLabel = "${invalidEmailLabel}";
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
            Label.captchaErrorLabel = "${captchaErrorLabel}";
        </script>
    </body>
</html>
