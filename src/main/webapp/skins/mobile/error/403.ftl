<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="403 Forbidden! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body class="error error-403">
        <#include "../header.ftl">
        <div class="slogan">
            <div class="wrapper block">
                <h2>${reloginLabel}</h2>
                <div class="slogan-border fn-clear">
                    <div class="slogan-register">
                        <button onclick="Util.goRegister()" class="green">${nowLabel}${registerLabel}</button>
                    </div>
                    <div class="slogan-text">
                        ${indexIntroLabel} &nbsp; &nbsp;
                        <a href="https://github.com/b3log/symphony" target="_blank" class="tooltipped tooltipped-n" aria-label="${siteCodeLabel}">
                            <svg><use xlink:href="#github"></use></svg></a> &nbsp;
                        <a href="http://weibo.com/u/2778228501" target="_blank" class="tooltipped tooltipped-n" aria-label="${followWeiboLabel}">
                            <svg><use xlink:href="#weibo"></use></svg></a>   &nbsp;
                        <a target="_blank" rel="noopener"
                           href="https://t.me/b3log">
                            <svg class="icon-telegram"><use xlink:href="#icon-telegram"></use></svg></a>
                    </div>
                </div>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div> 

        <#include '../footer.ftl'/>
    </body>
</html>
