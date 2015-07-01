<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${registerLabel}">
        <meta name="keywords" content="${registerLabel},${symphonyLabel}"/>
        <meta name="description" content="${registerLabel} ${symphonyLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear register content-reset">
                <div class="form fn-left">
                    <table cellpadding="0" cellspacing="0">
                        <tbody>
                            <tr>
                                <td width="70">
                                    <label for="userName">${userNameLabel}</label>
                                </td>
                                <td width="165">
                                    <input type="text" id="userName" />
                                    <span style="left: 275px; top: 14px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userEmail">${emailLabel}</label>
                                </td>
                                <td>
                                    <input type="text" id="userEmail" />
                                    <span style="left:275px;top:68px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for"userAppRole">${roleLabel}</label>
                                </td>
                                <td>
                                    <label><input name="userAppRole" type="radio" value="0" checked="checked" />&nbsp;&nbsp;${hackerLabel}</label>
                                    <label>&nbsp;&nbsp;&nbsp;&nbsp;<input name="userAppRole" type="radio" value="1" />&nbsp;&nbsp;${painterLabel}</label>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userPassword">${passwordLabel}</label>
                                </td>
                                <td>
                                    <input id="userPassword"  type="password" />
                                    <span style="left:275px;top:157px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="confirmPassword">${userPasswordLabel2}</label>
                                </td>
                                <td>
                                    <input id="confirmPassword" type="password" />
                                    <span style="left:275px;top:209px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="securityCode">${captchaLabel}</label>
                                </td>
                                <td>
                                    <input type="text" id="securityCode" />
                                    <span style="top:262px;left:275px;"></span>
                                    <img id="captcha" class="fn-pointer" src="/captcha" onclick="this.src = '/captcha?' + (new Date()).getTime()" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <span id="registerTip" style="top: 305px; right: 71px;"></span>
                                    <button class="green" onclick="Register.register()">${registerLabel}</button>
                                    <input id="referral" type="hidden" value="${referral}">
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="intro">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script type="text/javascript" src="/js/register${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="/js/lib/md5.js?${staticResourceVersion}"></script>
        <script>
                                        Register.init();
                                        Label.userNameErrorLabel = "${userNameErrorLabel}";
                                        Label.invalidEmailLabel = "${invalidEmailLabel}";
                                        Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
                                        Label.captchaErrorLabel = "${captchaErrorLabel}";
        </script>
    </body>
</html>
