<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${settingsLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div class="module">
                        <div class="module-header fn-clear">
                            <h2 class="fn-left">
                                <a rel="nofollow" href="/member/${user.userName}">${user.userName}</a>
                                ${profilesLabel}
                            </h2>&nbsp;
                            <span style="line-height:24px">(${user.userEmail})</span>
                        </div>
                        <div class="module-panel form fn-clear">
                            <label>URL</label><br/>
                            <input id="userURL" type="text" value="${user.userURL}"/>
                            <span style="top: 49px; right: 24px;"></span>

                            <label>QQ</label><br/>
                            <input id="userQQ" type="text" value="${user.userQQ}" />
                            <span style="right:24px;top:121px;"></span>

                            <label>${userIntroLabel}</label><br/>
                            <textarea id="userIntro">${user.userIntro}</textarea>
                            <span style="right:24px;top:193px;"></span><br/><br/>

                            <img class="avatar fn-left" src="${user.userThumbnailURL}">
                            <strong>
                                <a rel="friend" target="_blank" href="http://gravatar.com">${changeAvatarLabel}</a>
                            </strong>

                            <br/><br/>
                            <span id="profilesTip" style="right: 95px; top: 336px;"></span>
                            <button class="green fn-right" onclick="Settings.update('profiles')">${saveLabel}</button>
                        </div>
                    </div>

                    <div class="module">
                        <div class="module-header">
                            <h2>${syncLabel}</h2>
                        </div>
                        <div class="module-panel form fn-clear">
                            <label>B3log Key</label>
                            <input id="soloKey" type="text" value="${user.userB3Key}" /> 
                            <span style="right:24px;top:49px;"></span><br/>

                            <label>${clientArticleLabel}</label>
                            <input id="soloPostURL" type="text" value="${user.userB3ClientAddArticleURL}" />
                            <span style="right:24px;top:121px;"></span><br/>

                            <label>${clientUpdateArticleLabel}</label>
                            <input id="soloUpdateURL" type="text" value="${user.userB3ClientUpdateArticleURL}" />
                            <span style="right:24px;top:193px;"></span><br/>

                            <label>${clientCmtLabel}</label>
                            <input id="soloCmtURL" type="text" value="${user.userB3ClientAddCommentURL}" /> 
                            <span style="right:24px;top:265px;"></span><br/><br/>

                            <span id="syncb3Tip" style="right: 95px; top: 320px;"></span>
                            <button class="green fn-right" onclick="Settings.update('sync/b3')">${saveLabel}</button>
                        </div>
                    </div>

                    <div class="module">
                        <div class="module-header">
                            <h2>${passwordLabel}</h2>
                        </div>
                        <div class="module-panel form fn-clear">
                            <label>${oldPasswordLabel}</label>
                            <input id="pwdOld" type="password" />
                            <span style="right:24px;top:49px;"></span><br/>

                            <label>${newPasswordLabel}</label>
                            <input id="pwdNew" type="password" />
                            <span style="right:24px;top:121px;"></span><br/>

                            <label>${confirmPasswordLabel}</label>
                            <input id="pwdRepeat" type="password" /> 
                            <span style="right:24px;top:193px;"></span><br/><br/>

                            <span id="passwordTip" style="right: 95px; top: 320px;"></span>
                            <button class="green fn-right" onclick="Settings.update('password')">${saveLabel}</button>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#if ADLabel!="">
                    <div class="index-module">
                        ${ADLabel}
                    </div>
                    </#if>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="/js/settings.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="/js/lib/md5.js?${staticResourceVersion}"></script>
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
