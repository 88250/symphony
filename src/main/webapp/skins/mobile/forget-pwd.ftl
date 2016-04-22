<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${forgetPwdLabel} - ${symphonyLabel}">
        <meta name="description" content="${forgetPwdLabel} ${symphonyLabel}"/>
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
                                    <label for="userEmail">${emailLabel}</label>
                                </td>
                                <td>
                                    <input autofocus="autofocus" type="text" id="userEmail" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="securityCode">${captchaLabel}</label>
                                </td>
                                <td>
                                    <input type="text" id="securityCode" />
                                    <img id="captcha" class="fn-pointer" src="/captcha" onclick="this.src = '/captcha?' + (new Date()).getTime()" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <div id="registerTip" class="tip"></div><br/>
                                    <button class="green" onclick="Register.forgetPwd()">${forgetPwdLabel}</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <br/>
                <div class="intro fn-flex-1  content-reset">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/register${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/md5.js"></script>
        <script>
                                        Label.invalidEmailLabel = "${invalidEmailLabel}";
                                        Label.captchaErrorLabel = "${captchaErrorLabel}";
        </script>
    </body>
</html>
