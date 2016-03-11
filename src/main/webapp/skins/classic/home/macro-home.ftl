<#macro home type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "home">
        <@head title="${user.userName} - ${articleLabel} - ${symphonyLabel}">
        <meta name="description" content="<#list userHomeArticles as article><#if article_index<3>${article.articleTitle},</#if></#list>"/>
        </@head>
        <#elseif type == "comments">
        <@head title="${user.userName} - ${cmtLabel} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${cmtLabel}"/>
        </@head>
        <#elseif type == "followingUsers">
        <@head title="${user.userName} - ${followingUsersLabel} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingUsersLabel}"/>
        </@head>
        <#elseif type == "followingTags">
        <@head title="${user.userName} - ${followingTagsLabel} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingTagsLabel}"/>
        </@head>
        <#elseif type == "followingArticles">
        <@head title="${user.userName} - ${symphonyLabel} - ${followingArticlesLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingArticlesLabel}"/>
        </@head>
        <#elseif type == "followers">
        <@head title="${user.userName} - ${symphonyLabel} - ${followersLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followersLabel}"/>
        </@head>
        <#elseif type == "points">
        <@head title="${user.userName} - ${symphonyLabel} - ${pointLabel}">
        <meta name="description" content="${user.userName}${deLabel}${pointLabel}"/>
        </@head>
        <#elseif type == "settings">
        <@head title="${user.userName} - ${symphonyLabel} - ${settingsLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
        </@head>
        </#if>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
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
                        <li<#if type == "followingTags"> class="current"</#if>>
                            <a href="/member/${user.userName}/following/tags">${followingTagsLabel}</a>
                        </li>
                        <li<#if type == "followingArticles"> class="current"</#if>>
                            <a href="/member/${user.userName}/following/articles">${followingArticlesLabel}</a>
                        </li>
                        <li<#if type == "followers"> class="current"</#if>>
                            <a href="/member/${user.userName}/followers">${followersLabel}</a>
                        </li>
                        <li<#if type == "points"> class="current"</#if>>
                            <a href="/member/${user.userName}/points">${pointLabel}</a>
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
        <script type="text/javascript" src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Label.followLabel = "${followLabel}";
            Label.unfollowLabel = "${unfollowLabel}";
            Label.invalidPasswordLabel = "${invalidPasswordLabel}";
            Label.amountNotEmpty = "${amountNotEmpty}";
            Label.invalidUserNameLabel = "${invalidUserNameLabel}";
            Label.loginNameErrorLabel = "${loginNameErrorLabel}";
            Label.updateSuccLabel = "${updateSuccLabel}";
            Label.transferSuccLabel = "${transferSuccLabel}";
            Label.invalidUserURLLabel = "${invalidUserURLLabel}";
            Label.invalidAvatarURLLabel = "${invalidAvatarURLLabel}";
            Label.tagsErrorLabel = "${tagsErrorLabel}";
            Label.invalidUserQQLabel = "${invalidUserQQLabel}";
            Label.invalidUserIntroLabel = "${invalidUserIntroLabel}";
            Label.invalidUserB3KeyLabel = "${invalidUserB3KeyLabel}";
            Label.invalidUserB3ClientURLLabel = "${invalidUserB3ClientURLLabel}";
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
        </script>
    </body>
</html>
</#macro>
