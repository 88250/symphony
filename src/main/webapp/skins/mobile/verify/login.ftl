<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${loginLabel} - ${symphonyLabel}">
        <meta name="description" content="${registerLabel} ${symphonyLabel}"/>
        </@head>
        <link rel="canonical" href="${servePath}/register">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper verify">
                <div class="verify-wrap">
                    <div class="form">
                        <svg><use xlink:href="#logo"></use></svg>
                        <div class="input-wrap">
                            <svg><use xlink:href="#userrole"></use></svg>
                            <input id="nameOrEmail" type="text" autofocus="autofocus" placeholder="${nameOrEmailLabel}" autocomplete="off" />
                        </div>
                        <div class="input-wrap">
                            <svg><use xlink:href="#locked"></use></svg>
                            <input type="password" id="loginPassword" placeholder="${passwordLabel}" />
                        </div>
                        <div class="fn-none input-wrap">
                            <img id="captchaImg" class="captcha-img fn-pointer" />
                            <input type="text" id="captchaLogin" class="captcha-input" placeholder="${captchaLabel}" />
                        </div>

                        <div class="fn-clear">
                            <div class="fn-hr5"></div>
                            <input type="checkbox" id="rememberLogin" checked /> ${rememberLoginStatusLabel}
                            <a href="${servePath}/forget-pwd" class="fn-right">${forgetPwdLabel}</a>
                            <div class="fn-hr5"></div>
                        </div>
                        
                        <div id="loginTip" class="tip"></div>
                        <button class="green" onclick="Verify.login('${goto}')">${loginLabel}</button>
                        <button onclick="Util.goRegister()">${registerLabel}</button>
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
