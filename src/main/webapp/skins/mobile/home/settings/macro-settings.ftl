<#macro home type>
<#include "../../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${settingsLabel} - ${user.userName} - ${symphonyLabel}">
        <meta name="description" content="${user.userName}${deLabel}${settingsLabel}"/>
        </@head>
    </head>
    <body>
        <#include "../../header.ftl">
        <div class="main">
            <div class="tab-current fn-clear">
                <div class="fn-hr5"></div>
                <div onclick="$(this).next().next().slideToggle()">
                    <#if type == "profile">
                    ${profilesLabel}
                    <#elseif type == "avatar">
                    ${avatarLabel}
                    <#elseif type == "invite">
                    ${inviteLabel}
                    <#elseif type == "function">
                    ${functionLabel}
                    <#elseif type == "point">
                    ${pointLabel}
                    <#elseif type == "location">
                    ${geoLabel}
                    <#elseif type == "privacy">
                    ${privacyLabel}
                    <#elseif type == "password">
                    ${passwordLabel}
                    <#elseif type == "b3">
                    B3
                    <#elseif type == "i18n">
                    ${i18nLabel}
                    <#elseif type == "data">
                    ${dataLabel}
                    <#elseif type == "help">
                    ${helpLabel}
                    </#if>
                    <svg class="fn-right"><use xlink:href="#chevron-down"></use></svg>
                </div>
                <div class="fn-hr5"></div>
                <ul class="tab fn-clear fn-none">
                    <li<#if 'profile' == type> class="fn-none"</#if>><a href="${servePath}/settings">${profilesLabel}</a></li>
                    <li<#if 'avatar' == type> class="fn-none"</#if>><a href="${servePath}/settings/avatar">${avatarLabel}</a></li>
                    <li<#if 'invite' == type> class="fn-none"</#if>><a href="${servePath}/settings/invite">${inviteLabel}</a></li>
                    <li<#if 'function' == type> class="fn-none"</#if>><a href="${servePath}/settings/function">${functionLabel}</a></li>
                    <li<#if 'point' == type> class="fn-none"</#if>><a href="${servePath}/settings/point">${pointLabel}</a></li>
                    <li<#if 'location' == type> class="fn-none"</#if>><a href="${servePath}/settings/location">${geoLabel}</a></li>
                    <li<#if 'privacy' == type> class="fn-none"</#if>><a href="${servePath}/settings/privacy">${privacyLabel}</a></li>
                    <li<#if 'password' == type> class="fn-none"</#if>><a href="${servePath}/settings/password">${passwordLabel}</a></li>
                    <li<#if 'b3' == type> class="fn-none"</#if>><a href="${servePath}/settings/b3">B3</a></li>
                    <li<#if 'i18n' == type> class="fn-none"</#if>><a href="${servePath}/settings/i18n">${i18nLabel}</a></li>
                    <li<#if 'data' == type> class="fn-none"</#if>><a href="${servePath}/settings/data">${dataLabel}</a></li>
                    <li<#if 'help' == type> class="current"</#if>><a href="${servePath}/settings/help">${helpLabel}</a></li>
                </ul>
            </div>
            <div class="wrapper">
                <div class="fn-hr10"></div>
                <#nested>
            </div>
            <div class="side">
                <#include "../home-side.ftl">
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
