<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${resetPwdLabel} - ${symphonyLabel}">
        <meta name="description" content="${resetPwdLabel} ${symphonyLabel}"/>
        </@head>
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
                                <td colspan="2" align="right">
                                    <div id="registerTip" class="tip"></div><br/>
                                    <button class="green" onclick="Register.resetPwd()">${resetPwdLabel}</button>
                                    <input id="userId" type="hidden" value="${user.oId}">
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="intro content-reset"><br/>
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
