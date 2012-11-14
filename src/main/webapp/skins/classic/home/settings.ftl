<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${settingsLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper form">
                <div class="module">
                    <div class="module-header fn-clear">
                        <h2 class="fn-left">
                            <a rel="nofollow" href="/member/${user.userName}">${user.userName}</a>
                            ${profilesLabel}
                        </h2>&nbsp;
                        <span style="line-height:24px">(${user.userEmail})</span>
                    </div>
                    <div class="module-panel form">
                        <table>
                            <tr>
                                <td width="100">
                                    URL
                                </td>
                                <td>
                                    <input id="userURL" type="text" value="${user.userURL}"/>
                                    <span style="right:24px;top:33px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    QQ
                                </td>
                                <td>
                                    <input id="userQQ" type="text" value="${user.userQQ}" />
                                    <span style="right:24px;top:85px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${userIntroLabel}
                                </td>
                                <td>
                                    <textarea id="userIntro">${user.userIntro}</textarea>
                                    <span style="right:24px;top:137px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <img class="avatar fn-left" src="${user.userThumbnailURL}">
                                </td>
                                <td>
                                    <a rel="friend" target="_blank" href="http://gravatar.com">${changeAvatarLabel}</a>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <span id="profilesTip" style="left:20px;top:268px;"></span>
                                    <button class="green fn-right" onclick="Settings.update('profiles')">${saveLabel}</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>

                <div class="module">
                    <div class="module-header">
                        <h2>${syncLabel}</h2>
                    </div>
                    <div class="module-panel form">
                        <table>
                            <tr>
                                <td width="100">
                                    B3log Key
                                </td>
                                <td>
                                    <input id="soloKey" type="text" value="${user.userB3Key}" /> 
                                    <span style="right:24px;top:33px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${clientArticleLabel}
                                </td>
                                <td>
                                    <input id="soloPostURL" type="text" value="${user.userB3ClientAddArticleURL}" />
                                    <span style="right:24px;top:85px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${clientCmtLabel}
                                </td>
                                <td>
                                    <input id="soloCmtURL" type="text" value="${user.userB3ClientAddCommentURL}" /> 
                                    <span style="right:24px;top:137px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <span id="syncb3Tip" style="left:20px;top:180px;"></span>
                                    <button class="green" onclick="Settings.update('sync/b3')">${saveLabel}</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>

                <div class="module">
                    <div class="module-header">
                        <h2>${passwordLabel}</h2>
                    </div>
                    <div class="module-panel form">
                        <table>
                            <tr>
                                <td width="100">
                                    ${oldPasswordLabel}
                                </td>
                                <td>
                                    <input id="pwdOld" type="password" />
                                    <span style="right:24px;top:33px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${newPasswordLabel}
                                </td>
                                <td>
                                    <input id="pwdNew" type="password" />
                                    <span style="right:24px;top:85px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    ${confirmPasswordLabel}
                                </td>
                                <td>
                                    <input id="pwdRepeat" type="password" /> 
                                    <span style="right:24px;top:137px;"></span>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="right">
                                    <span id="passwordTip" style="left:20px;top:180px;"></span>
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
        <script>
            Label.updateSuccLabel = "${updateSuccLabel}";
            Label.invalidUserURLLabel = "${invalidUserURLLabel}";
            Label.invalidUserQQLabel = "${invalidUserQQLabel}";
            Label.invalidUserIntroLabel = "${invalidUserIntroLabel}";
            Label.invalidUserB3KeyLabel = "${invalidUserB3KeyLabel}";
            Label.invalidUserB3ClientURLLabel = "${invalidUserB3ClientURLLabel}";
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
        </script>
    </body>
</html>
