<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="404 Not Found! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        <link rel="stylesheet" href="${staticServePath}/css/error.css?${staticResourceVersion}" />
        </@head>
    </head>
    <body class="error">
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="module article-module">
                <h2 class="sub-head">404 Not Found!</h2>
                <div class="ft-center"></div>
                </div>
            </div>
        </div>
        <#include '../footer.ftl'/>
    </body>
    <script>
        $('.main .wrapper div.ft-center').html('<img src="${staticServePath}/images/404/' + Math.round(Math.random() * 6) + '.gif">');
        Util.mouseClickEffects();
    </script>
</html>