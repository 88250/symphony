<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="404 Not Found! - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main"  style="margin-left: auto;margin-right: auto;">
            <div class="wrapper">
                <div class="content">
                    <h2>404 Not Found!</h2>
                    <br>
                    <script type="text/javascript">
                        var index = Math.round(Math.random() * 6);
                        document.write('<img src="${staticServePath}/images/404/404_' + index + '.gif">');
                    </script>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>