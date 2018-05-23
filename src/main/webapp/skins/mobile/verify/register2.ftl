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
        <@head title="${registerLabel} - ${symphonyLabel}">
        <meta name="description" content="${registerLabel} ${symphonyLabel}"/>
        </@head>
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
                            <input type="text" id="registerUserName2" value="${user.userName}" readonly="readonly" placeholder="${userNameLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <svg><use xlink:href="#email"></use></svg>
                            <input type="text" id="registerUserEmail2" value="${user.userEmail}" readonly="readonly" placeholder="${emailLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <svg><use xlink:href="#locked"></use></svg>
                            <input type="password" autofocus="autofocus" id="registerUserPassword2" placeholder="${passwordLabel}" />
                        </div>
                         <div class="input-wrap">
                             <svg><use xlink:href="#locked"></use></svg>
                            <input type="password" id="registerConfirmPassword2" placeholder="${userPasswordLabel2}" />
                        </div>
                        <div class="fn-clear">
                            <label>${roleLabel}</label>
                            <label>&nbsp;&nbsp;&nbsp;&nbsp;<input name="userAppRole" type="radio" value="0" checked="checked" />&nbsp;&nbsp;${programmerLabel}</label>
                            <label style="float:right">&nbsp;&nbsp;<input name="userAppRole" type="radio" value="1" />&nbsp;&nbsp;${designerLabel}</label>

                        </div>
                        <div id="registerTip2" class="tip"></div>
                        <button class="green" onclick="Verify.register2()">${registerLabel}</button>
                        <input id="referral2" type="hidden" value="${referral}">
                        <input id="userId2" type="hidden" value="${user.oId}">

                    </div>
                </div>
                <div class="intro fn-flex-1 content-reset">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/verify${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Verify.init();
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
        </script>
    </body>
</html>
