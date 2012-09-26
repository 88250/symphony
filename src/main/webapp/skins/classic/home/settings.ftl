<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper form">
                <table>
                    <tr>
                        <td width="100">
                            UserName
                        </td>
                        <td>
                            <input id="userName" type="text" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            UserURL
                        </td>
                        <td>
                            <input id="userURL" type="text" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            User Email
                        </td>
                        <td>
                            <input id="userEmail" type="text" /> 
                        </td>
                    </tr>
                    <tr>
                        <td>
                            QQ
                        </td>
                        <td>
                            <input id="userQQ" type="text" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            Intro
                        </td>
                        <td>
                            <textarea id="userIntro"></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <button class="green">Save</button>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
