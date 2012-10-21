<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel}">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear register">
                <div class="form fn-left">
                    <table cellpadding="0" cellspacing="0">
                        <caption>
                            <h2>${registerLabel}</h2>
                        </caption>
                        <tbody>
                            <tr>
                                <td>
                                    <label for="userName">${userNameLabel}</label>
                                </td>
                                <td>
                                    <input type="text" placeholder="Username" id="userName" />
                                    <span style="left:280px;top:38px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userEmail">${emailLabel}</label>
                                </td>
                                <td>
                                    <input type="text" id="userEmail" />
                                    <span style="left:280px;top:86px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userPassword">${passwordLabel}</label>
                                </td>
                                <td>
                                    <input id="userPassword"  type="password" />
                                    <span style="left:280px;top:134px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="confirmPassword">${userPasswordLabel2}</label>
                                </td>
                                <td>
                                    <input id="confirmPassword" type="password" />
                                    <span style="left:280px;top:182px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="securityCode">${captchaLabel}</label>
                                </td>
                                <td>
                                    <input type="text" id="securityCode" />
                                    <span style="left:280px;top:230px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <span id="registerTip"></span>
                                    <button onclick="Register.register()">${registerLabel}</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="fn-left intro">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script type="text/javascript" src="/js/register.js"></script>
        <script>
            Register.init();
        </script>
    </body>
</html>
