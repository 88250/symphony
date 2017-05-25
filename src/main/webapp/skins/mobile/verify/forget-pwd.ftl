<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${forgetPwdLabel} - ${symphonyLabel}">
        <meta name="description" content="${forgetPwdLabel} ${symphonyLabel}"/>
        </@head>
        <link rel="canonical" href="${servePath}/forget-pwd">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper verify">
                <div class="verify-wrap">
                    <div class="form">
                        <svg><use xlink:href="#logo"></use></svg>
                        <div class="input-wrap">
                            <svg><use xlink:href="#email"></use></svg>
                            <input id="fpwdEmail" type="text" placeholder="${emailLabel}" autocomplete="off" autofocus="autofocus" />
                        </div>
                        <div class="input-wrap">
                            <img id="fpwdCaptcha" class="fn-pointer captcha-img" src="${servePath}/captcha" onclick="this.src = '${servePath}/captcha?' + (new Date()).getTime()" />
                            <input type="text" id="fpwdSecurityCode" class="captcha-input" placeholder="${captchaLabel}" />
                        </div>
                        <div id="fpwdTip" class="tip"></div>
                        <button onclick="Verify.forgetPwd()">${forgetPwdLabel}</button>
                        <button class="green" onclick="Util.goLogin()">${loginLabel}</button>
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
            Label.invalidEmailLabel = "${invalidEmailLabel}";
            Label.captchaErrorLabel = "${captchaErrorLabel}";
        </script>
    </body>
</html>
