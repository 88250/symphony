<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${resetPwdLabel} - ${symphonyLabel}">
        <meta name="description" content="${resetPwdLabel} ${symphonyLabel}"/>
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper verify">
                <div class="verify-wrap">
                    <div class="form">
                        ${logoIcon2}

                        <div class="input-wrap">
                            <span class="icon-userrole"></span>
                            <input type="text" id="rpwdrUserName" value="${user.userName}" readonly="readonly" placeholder="${userNameLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <span class="icon-email"></span>
                            <input type="text" id="rpwdUserEmail" value="${user.userEmail}" readonly="readonly" placeholder="${emailLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <span class="icon-locked"></span>
                            <input type="password" autofocus="autofocus" id="rpwdUserPassword" placeholder="${passwordLabel}" />
                        </div>
                         <div class="input-wrap">
                            <span class="icon-locked"></span>
                            <input type="password" id="rpwdConfirmPassword" placeholder="${userPasswordLabel2}" />
                        </div>
                         <div id="rpwdTip" class="tip"></div>
                        <button class="green" onclick="Verify.resetPwd()">${resetPwdLabel}</button>
                        <input id="rpwdUserId" type="hidden" value="${user.oId}">
                    </div>
                </div>
                <div class="intro fn-flex-1 content-reset">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/verify${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                        Verify.init();
                                        Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
        </script>
    </body>
</html>
