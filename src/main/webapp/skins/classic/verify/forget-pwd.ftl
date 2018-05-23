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
        <@head title="${forgetPwdLabel} - ${symphonyLabel}">
        <meta name="description" content="${forgetPwdLabel} ${symphonyLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/forget-pwd">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper verify">
                <div class="verify-wrap">
                    <div class="form">
                        <svg><use xlink:href="#logo"></use></svg>

                        <div class="input-wrap">
                            <svg><use xlink:href="#email"></use></svg>
                            <input id="fpwdEmail" type="text" placeholder="${emailLabel}" autocomplete="off" autofocus="autofocus" />
                        </div>
                        <div class="input-wrap">
                            <img id="fpwdCaptcha" class="fn-pointer captcha-img" src="${servePath}/captcha" onclick="this.src = '${servePath}/captcha?' + (new Date()).getTime()" />
                            <input type="text" id="fpwdSecurityCode" class="captcha-input" placeholder="${captchaLabel}" />
                        </div>
                        <div id="fpwdTip" class="tip"></div>
                        <button class="green" onclick="Verify.forgetPwd()">${forgetPwdLabel}</button>
                        <button onclick="Util.goLogin()">${loginLabel}</button>
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
            Label.invalidEmailLabel = "${invalidEmailLabel}";
            Label.captchaErrorLabel = "${captchaErrorLabel}";
        </script>
    </body>
</html>
