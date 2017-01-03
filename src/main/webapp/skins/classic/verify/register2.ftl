<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${registerLabel} - ${symphonyLabel}">
        <meta name="description" content="${registerLabel} ${symphonyLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
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
                            <input type="text" id="registerUserName2" value="${user.userName}" readonly="readonly" placeholder="${userNameLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <span class="icon-email"></span>
                            <input type="text" id="registerUserEmail2" value="${user.userEmail}" readonly="readonly" placeholder="${emailLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <span class="icon-locked"></span>
                            <input type="password" autofocus="autofocus" id="registerUserPassword2" placeholder="${passwordLabel}" />
                        </div>
                         <div class="input-wrap">
                            <span class="icon-locked"></span>
                            <input type="password" id="registerConfirmPassword2" placeholder="${userPasswordLabel2}" />
                        </div>
                        <div class="fn-clear">
                            <label>${roleLabel}</label>
                            <label>&nbsp;&nbsp;&nbsp;&nbsp;<input name="userAppRole" type="radio" value="0" checked="checked" />&nbsp;&nbsp;${programmerLabel}</label>
                            <label style="float:right">&nbsp;&nbsp;<input name="userAppRole" type="radio" value="1" />&nbsp;&nbsp;${designerLabel}</label>

                        </div>
                        <div id="registerTip2" class="tip"></div>
                        <button class="green" onclick="Verify.register2()">${registerLabel}</button>
                        <input id="referral2" type="hidden" value="${referral}">
                        <input id="userId2" type="hidden" value="${user.oId}">

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
