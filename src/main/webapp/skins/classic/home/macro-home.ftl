<#macro home type>
<#include "../macro-head.ftl">
<#include "../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "home">
        <@head title="${user.userName} - ${articleLabel}">
        <meta name="keywords" content="${user.userName},${articleLabel}"/>
        <meta name="description" content="<#list userHomeArticles as article><#if article_index<3>${article.articleTitle},</#if></#list>"/>
        </@head>
        <#elseif type == "comments">
        <@head title="${user.userName} - ${cmtLabel}">
        <meta name="keywords" content="${user.userName},${cmtLabel}"/>
        <meta name="description" content="${user.userName}${deLabel}${cmtLabel},${cmtLabel} by ${user.userName}"/>
        </@head>
        </#if>
        <link type="text/css" rel="stylesheet" href="/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <ul class="tab fn-clear">
                        <li<#if type == "home"> class="current"</#if>>
                            <a href="/member/${user.userName}">${articleLabel}</a>
                        </li>
                        <li<#if type == "comments"> class="current"</#if>>
                            <a href="/member/${user.userName}/comments">${cmtLabel}</a>
                        </li>
                    </ul>
                    <div class="fn-clear">
                        <#nested>
                    </div>
                </div>
                <div class="side">
                    <#include "home-side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script>
                                var Label = {
                                    followLabel: "${followLabel}",
                                    unfollowLabel: "${unfollowLabel}"
                                };
        </script>
    </body>
</html>
</#macro>
