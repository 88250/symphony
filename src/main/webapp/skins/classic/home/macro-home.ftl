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
        <#elseif type == "followingUsers">
        <@head title="${user.userName} - ${followingUsersLabel}">
        <meta name="keywords" content="${user.userName},${followingUsersLabel}"/>
        <meta name="description" content="${user.userName}${deLabel}${followingUsersLabel},${followingUsersLabel} by ${user.userName}"/>
        </@head>
        <#elseif type == "followers">
        <@head title="${user.userName} - ${followersLabel}">
        <meta name="keywords" content="${user.userName},${followersLabel}"/>
        <meta name="description" content="${user.userName}${deLabel}${followersLabel},${followersLabel} by ${user.userName}"/>
        </@head>
        <#elseif type == "settings">
        <@head title="${user.userName} - ${settingsLabel}">
        <meta name="keywords" content="${user.userName},${settingsLabel}"/>
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel},${settingsLabel} by ${user.userName}"/>
        </@head>
        </#if>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
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
                        <li<#if type == "followingUsers"> class="current"</#if>>
                            <a href="/member/${user.userName}/following/users">${followingUsersLabel}</a>
                        </li>
                        <li<#if type == "followers"> class="current"</#if>>
                            <a href="/member/${user.userName}/followers">${followersLabel}</a>
                        </li>
                        <#if currentUser?? && currentUser.userName == user.userName>
                        <li<#if type == "settings"> class="current"</#if>>
                            <a href="/settings">${settingsLabel}</a>
                        </li>
                        </#if>
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
        <link type="text/css" rel="stylesheet" href="/css/home${miniPostfix}.css?${staticResourceVersion}" />
        <script type="text/javascript" src="/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Util.init();
            Label.followLabel = "${followLabel}";
            Label.unfollowLabel = "${unfollowLabel}";
            Label.invalidPasswordLabel = "${invalidPasswordLabel}";
            Label.loginNameErrorLabel = "${loginNameErrorLabel}";
            Label.updateSuccLabel = "${updateSuccLabel}";
            Label.invalidUserURLLabel = "${invalidUserURLLabel}";
            Label.invalidUserQQLabel = "${invalidUserQQLabel}";
            Label.invalidUserIntroLabel = "${invalidUserIntroLabel}";
            Label.invalidUserB3KeyLabel = "${invalidUserB3KeyLabel}";
            Label.invalidUserB3ClientURLLabel = "${invalidUserB3ClientURLLabel}";
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
        </script>
    </body>
</html>
</#macro>
