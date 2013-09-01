<#include "../../macro-head.ftl">
<#include "../../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - block">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <ul>
                        <#list commenteds as cmt>
                        <li>
                            ${cmt}
                        </li>
                        </#list>
                    </ul>
                </div>
                <div class="side">
                    <ul class="note-list">
                        <li class="current">
                            <a href="/notifications/commented">commted</a> 
                        </li>
                        <li>
                            <a href="/notifications/commented">commted</a>  
                        </li>
                        <li>
                            <a href="/notifications/commented">commted</a>  
                        </li>
                        <li>
                            <a href="/notifications/commented">commted</a>  
                        </li>
                    </ul>
                     <@pagination url="/notifications/commented"/>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
    </body>
</html>
