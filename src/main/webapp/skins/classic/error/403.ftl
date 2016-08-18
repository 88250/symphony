<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="403 Forbidden! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <h2>${reloginLabel}</h2>
                <div class="fn-hr10"></div>
            </div>
        </div>
        <#include '../common/footer-dom.ftl'/>
    </body>
</html>