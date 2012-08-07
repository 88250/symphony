<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
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
                                    <span id="tip"></span>
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
        <#include "footer.ftl">
        <script type="text/javascript" src="/js/register.js"></script>
        <script>
            Label.sss = "";
        </script>
    </body>
</html>
