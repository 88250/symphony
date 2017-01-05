<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${registerLabel} - ${symphonyLabel}">
        <meta name="description" content="${registerLabel} ${symphonyLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/register">
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
                            <input id="registerUserName" type="text" placeholder="${userNamePlaceholderLabel}" autocomplete="off" autofocus="autofocus" />
                        </div>
                        <div class="input-wrap">
                            <span class="icon-email"></span>
                            <input id="registerUserEmail" type="text" placeholder="${emailPlaceholderLabel}" autocomplete="off" />
                        </div>
                        
                        <div class="input-wrap<#if "2" != miscAllowRegister> fn-none</#if>">
                            <span class="icon-heart"></span>
                            <input id="registerInviteCode" type="text" placeholder="${invitecodePlaceholderLabel}" autocomplete="off" />
                        </div>
                        
                        
                        <div class="input-wrap<#if "2" == miscAllowRegister> fn-none</#if>">
                            <img id="registerCaptchaImg" class="fn-pointer captcha-img " src="${servePath}/captcha" onclick="this.src = '${servePath}/captcha?' + (new Date()).getTime()" />
                            <input type="text" id="registerCaptcha" class="captcha-input" placeholder="${captchaLabel}" />
                        </div>
                       
                        <div id="registerTip" class="tip"></div>
                        <input id="referral" type="hidden" value="${referral}">
                        <button class="green" id="registerBtn" onclick="Verify.register()">${registerLabel}</button>
                        <button onclick="Util.goLogin()">${loginLabel}</button>
                    </div>
                </div>
                <div class="intro content-reset">
                    ${introLabel}
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/verify${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Verify.init();
            Label.userNameErrorLabel = "${userNameErrorLabel}";
            Label.invalidEmailLabel = "${invalidEmailLabel}";
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
            Label.captchaErrorLabel = "${captchaErrorLabel}";
        </script>
    </body>
</html>
