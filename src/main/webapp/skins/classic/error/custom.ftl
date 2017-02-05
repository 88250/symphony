<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="Oops! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="module article-module">
                    <h2 class="sub-head">Oops!</h2>
                    <div class="no-list">${msg}</div>
                </div>
            </div>
        </div>
        <#include '../footer.ftl'/>
    </body>
</html>