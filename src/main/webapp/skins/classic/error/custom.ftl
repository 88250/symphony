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
            <div class="wrapper block">
                <h2 class="sub-head">Oops!</h2><br/>
                ${msg}
            </div>
        </div>
        <#include '../footer.ftl'/>
    </body>
</html>