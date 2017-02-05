<#macro home type>
<#include "../../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${settingsLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <#nested>
                </div>
                <div class="side">
                    <#if 'profile' == type || 'avatar' == type>
                        <div id="homeSidePanel" class="fn-none">
                            <#include "../home-side.ftl">
                        </div>
                    </#if>
                    <div class="module">
                        <div class="module-header"><h2>${goHomeLabel}</h2></div> 
                        <div class="module-panel fn-oh">
                            <nav class="home-menu">
                                <a <#if type == "home" || type == "comments" || type == "articlesAnonymous" || type == "commentsAnonymous">
                                    class="current"</#if>
                                    href="${servePath}/member/${user.userName}"><svg height="18" viewBox="0 1 16 16" width="16">${boolIcon}</svg> ${postLabel}</a>
                                <a <#if type == "followingUsers" || type == "followingTags" || type == "followingArticles" || type == "followers"> class="current"</#if>
                                    href="${servePath}/member/${user.userName}/watching/articles"><svg height="18" viewBox="0 1 14 16" width="14">${starIcon}</svg> ${followLabel}</a>
                                <a <#if type == "points"> class="current"</#if> href="${servePath}/member/${user.userName}/points">
                                    <svg height="18" viewBox="0 1 14 16" width="14">${giftIcon}</svg> ${pointLabel}</a>
                                <a <#if type == "linkForge"> class="current"</#if> href="${servePath}/member/${user.userName}/forge/link">
                                    <svg height="18" viewBox="0 1 16 16" width="16">${baguaIcon}</svg>  ${forgeLabel}</a>
                            </nav>
                        </div>
                    </div>
                    <div class="module">
                        <div class="module-header"><h2>${settingsLabel}</h2></div> 
                        <div class="module-panel fn-oh">
                            <nav class="home-menu">
                                <a href="${servePath}/settings"<#if 'profile' == type> class="current"</#if>>${profilesLabel}</a>
                                <a href="${servePath}/settings/avatar"<#if 'avatar' == type> class="current"</#if>>
                                   ${avatarLabel}
                                   <#if !currentUser.userAvatarURL?contains('_')><span class="todo tooltipped tooltipped-w" aria-label="${todoAvatarLabel}"></span></#if>
                                </a>
                                <a href="${servePath}/settings/invite"<#if 'invite' == type> class="current"</#if>>
                                       ${inviteLabel}
                                       <#if invitedUserCnt < 1><span class="todo tooltipped tooltipped-w" aria-label="${todoInviteLabel}"></span></#if>
                                </a>
                                <a href="${servePath}/settings/function"<#if 'function' == type> class="current"</#if>>${functionLabel}</a>
                                <a href="${servePath}/settings/point"<#if 'point' == type> class="current"</#if>>${pointLabel}</a>
                                <a href="${servePath}/settings/location"<#if 'location' == type> class="current"</#if>>${geoLabel}</a>
                                <a href="${servePath}/settings/privacy"<#if 'privacy' == type> class="current"</#if>>${privacyLabel}</a>
                                <a href="${servePath}/settings/password"<#if 'password' == type> class="current"</#if>>${passwordLabel}</a>
                                <a href="${servePath}/settings/b3"<#if 'b3' == type> class="current"</#if>>B3</a>
                                <a href="${servePath}/settings/data"<#if 'data' == type> class="current"</#if>>${dataLabel}</a>
                                <a href="${servePath}/settings/i18n"<#if 'i18n' == type> class="current"</#if>>${i18nLabel}</a>
                                <a href="${servePath}/settings/help"<#if 'help' == type> class="current"</#if>>${helpLabel}</a>
                            </nav>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "../../footer.ftl">
        <script src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
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
            Label.tagsErrorLabel = "${tagsErrorLabel}";
            Label.invalidUserQQLabel = "${invalidUserQQLabel}";
            Label.invalidUserIntroLabel = "${invalidUserIntroLabel}";
            Label.invalidUserB3KeyLabel = "${invalidUserB3KeyLabel}";
            Label.invalidUserB3ClientURLLabel = "${invalidUserB3ClientURLLabel}";
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
            Label.invalidUserNicknameLabel = "${invalidUserNicknameLabel}";
            Label.previewLabel = "${previewLabel}";
            Label.unPreviewLabel = "${unPreviewLabel}";
        </script>
    </body>
</html>
</#macro>
