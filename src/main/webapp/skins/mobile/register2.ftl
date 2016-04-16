<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${registerLabel} - ${symphonyLabel}">
        <meta name="description" content="${registerLabel} ${symphonyLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper register">
                <div class="form">
                    <table cellpadding="0" cellspacing="0">
                        <tbody>
                            <tr>
                                <td width="70">
                                    <label for="userName">${userNameLabel}</label>
                                </td>
                                <td width="165">
                                    <input type="text" id="userName" value="${user.userName}" readonly="readonly" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userEmail">${emailLabel}</label>
                                </td>
                                <td>
                                    <input type="text" id="userEmail" value="${user.userEmail}" readonly="readonly" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userPassword">${passwordLabel}</label>
                                </td>
                                <td>
                                    <input id="userPassword"  type="password" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="confirmPassword">${userPasswordLabel2}</label>
                                </td>
                                <td>
                                    <input id="confirmPassword" type="password" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for"userAppRole">${roleLabel}</label>
                                </td>
                                <td>
                                    <label><input name="userAppRole" type="radio" value="0" checked="checked" />&nbsp;&nbsp;${programmerLabel}</label>
                                    <label>&nbsp;&nbsp;&nbsp;&nbsp;<input name="userAppRole" type="radio" value="1" />&nbsp;&nbsp;${designerLabel}</label>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right"><br/>
                                    <div id="registerTip" class="tip"></div><br/>
                                    <button class="green" onclick="Register.register2()">${registerLabel}</button>
                                    <input id="referral" type="hidden" value="${referral}">
                                    <input id="userId" type="hidden" value="${user.oId}">
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="intro fn-flex-1 content-reset">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/register${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/md5.js"></script>
        <script>
                                        Register.init2();
                                        Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
        </script>
    </body>
</html>
