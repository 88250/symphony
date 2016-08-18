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
                 <h2>${msg}</h2>
            </div>
        </div>
        <#include '../common/footer-dom.ftl'/>
    </body>
</html>