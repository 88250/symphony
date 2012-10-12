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
                <div class="module">
                    <div class="module-header fn-clear">
                        <h2 class="fn-left">${profilesLabel}</h2>
                    </div>
                    <div class="module-panel">
                        <table>
                            <tr>
                                <td width="100">
                                    ${userNameLabel}
                                </td>
                                <td>
                                    <input id="userName" type="text" value="${user.userName}" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    URL
                                </td>
                                <td>
                                    <input id="userURL" type="text" value="${user.userURL}"/>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${emailLabel}
                                </td>
                                <td>
                                    <input id="userEmail" type="text" value="${user.userEmail}" readonly="readonly" /> 
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    QQ
                                </td>
                                <td>
                                    <input id="userQQ" type="text" value="${user.userQQ}" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${introLabel}
                                </td>
                                <td>
                                    <textarea id="userIntro">${user.userIntro}</textarea>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <img class="avatar fn-left" src="${user.userThumbnailURL}">
                                </td>
                                <td>
                                    <a target="_blank" href="http://gravatar.com">${changeAvatarLabel}</a>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <div class="fn-clear">
                                        <button class="green fn-right" onclick="Settings.update('profiles')">${saveLabel}</button>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>

                <div class="module">
                    <div class="module-header fn-clear">
                        <h2 class="fn-left">${syncLabel}</h2>
                    </div>
                    <div class="module-panel">
                        <table>
                            <tr>
                                <td width="100">
                                    B3log Key
                                </td>
                                <td>
                                    <input id="soloKey" type="text" value="${user.userB3Key}" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${clientArticleLabel}
                                </td>
                                <td>
                                    <input id="soloPostURL" type="text" value="${user.userB3ClientAddArticleURL}" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${clientCmtLabel}
                                </td>
                                <td>
                                    <input id="soloCmtURL" type="text" value="${user.userB3ClientAddCommentURL}" /> 
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <button class="green" onclick="Settings.update('sync/b3')">${saveLabel}</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>

                <div class="module">
                    <div class="module-header fn-clear">
                        <h2 class="fn-left">${passwordLabel}</h2>
                    </div>
                    <div class="module-panel">
                        <table>
                            <tr>
                                <td width="100">
                                    ${oldPasswordLabel}
                                </td>
                                <td>
                                    <input id="pwdOld" type="password" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${newPasswordLabel}
                                </td>
                                <td>
                                    <input id="pwdNew" type="password" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${confirmPasswordLabel}
                                </td>
                                <td>
                                    <input id="pwdRepeat" type="password" /> 
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <button class="green" onclick="Settings.update('password')">${saveLabel}</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="/js/settings.js"></script>
    </body>
</html>
