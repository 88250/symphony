<#include "macro-head.ftl">
<#include "macro-footer.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />f
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear register">
                <div class="form fn-left">
                    <table>
                        <caption>
                            <h2>Register</h2>
                        </caption>
                        <tbody>
                            <tr>
                                <td>
                                    <label for="userName">UseName</label>
                                </td>
                                <td>
                                    <input placeholder="UseName" id="userName" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userEmail">userEmail</label>
                                </td>
                                <td>
                                    <input id="userEmail" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="userPassword">UserPassword</label>
                                </td>
                                <td>
                                    <input id="userPassword"  type="password" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="confirmPassword">RepeatPassword</label>
                                </td>
                                <td>
                                    <input id="confirmPassword" type="password" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="securityCode">验证码</label>
                                </td>
                                <td>
                                    <input id="securityCode" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <span id="registerTip"></span>
                                </td>
                                <td align="right">
                                    <button onclick="Register.register()">Register</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="fn-left intro">
                    intro
                </div>
            </div>
        </div>
        <@footer/>
        <script type="text/javascript" src="/js/register.js"></script>
    </body>
</html>
