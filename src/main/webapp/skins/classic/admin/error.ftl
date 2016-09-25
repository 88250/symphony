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
                <div class="module ">
                    <div class="no-list">${msg}</div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>