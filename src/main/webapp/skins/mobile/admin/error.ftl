<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${msg} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <div class="content">
                    ${msg}
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>