<#macro home type>
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <#if type == "home">
        <@head title="${articleLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="<#list userHomeArticles as article><#if article_index<3>${article.articleTitle},</#if></#list>"/>
        </@head>
        <#elseif type == "comments">
        <@head title="${cmtLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${cmtLabel}"/>
        </@head>
        <#elseif type == "followingUsers">
        <@head title="${followingUsersLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingUsersLabel}"/>
        </@head>
        <#elseif type == "followingTags">
        <@head title="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingTagsLabel}"/>
        </@head>
        <#elseif type == "followingArticles">
        <@head title="${followingArticlesLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followingArticlesLabel}"/>
        </@head>
        <#elseif type == "followers">
        <@head title="${followersLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${followersLabel}"/>
        </@head>
        <#elseif type == "points">
        <@head title="${pointLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${pointLabel}"/>
        </@head>
        <#elseif type == "settings">
        <@head title="${settingsLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
        </@head>
        </#if>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="tab-current fn-clear">
                <div onclick="$(this).next().next().toggle()">
                    <#if type == "home">
                    ${articleLabel}
                    <#elseif type == "comments">
                    ${cmtLabel}
                    <#elseif type == "followingUsers">
                    ${followingUsersLabel}
                    <#elseif type == "followingTags">
                    ${followingTagsLabel}
                    <#elseif type == "followingArticles">
                    ${followingArticlesLabel}
                    <#elseif type == "followers">
                    ${followersLabel}
                    <#elseif type == "points">
                    ${pointLabel}
                    <#elseif type == "settings">
                    ${settingsLabel}
                    </#if>
                    <span class="icon-chevron-down fn-right"></span>
                </div>
                <div class="fn-hr5"></div>
                <ul class="tab fn-clear fn-none">
                    <li<#if type == "home"> class="fn-none"</#if>>
                        <a href="/member/${user.userName}">${articleLabel}</a>
                    </li>
                    <li<#if type == "comments"> class="fn-none"</#if>>
                        <a href="/member/${user.userName}/comments">${cmtLabel}</a>
                    </li>
                    <li<#if type == "followingUsers"> class="fn-none"</#if>>
                        <a href="/member/${user.userName}/following/users">${followingUsersLabel}</a>
                    </li>
                    <li<#if type == "followingTags"> class="fn-none"</#if>>
                        <a href="/member/${user.userName}/following/tags">${followingTagsLabel}</a>
                    </li>
                    <li<#if type == "followingArticles"> class="fn-none"</#if>>
                        <a href="/member/${user.userName}/following/articles">${followingArticlesLabel}</a>
                    </li>
                    <li<#if type == "followers"> class="fn-none"</#if>>
                        <a href="/member/${user.userName}/followers">${followersLabel}</a>
                    </li>
                    <li<#if type == "points"> class="fn-none"</#if>>
                        <a href="/member/${user.userName}/points">${pointLabel}</a>
                    </li>
                    <#if currentUser?? && currentUser.userName == user.userName>
                    <li<#if type == "settings"> class="fn-none"</#if>>
                        <a href="/settings"><b class="ft-red">${settingsLabel}</b></a>
                    </li>
                    </#if>
                </ul>
            </div>
            <div class="fn-clear">
                <#nested>
            </div>
            <div class="side">
                <#include "home-side.ftl">
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
                    Label.invalidUserNicknameLabel = "${invalidUserNicknameLabel}";
        </script>
    </body>
</html>
</#macro>
