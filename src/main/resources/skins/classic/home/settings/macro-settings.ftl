<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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
    <body class="fn__flex-column">
        <#include "../../header.ftl">
        <div class="main fn__flex-1">
            <div class="wrapper">
                <div class="content">
                    <#nested>
                </div>
                <div class="side">
                    <div class="module">
                        <div class="module-panel fn-oh">
                            <nav class="home-menu">
                                <a href="${servePath}/settings"<#if 'profile' == type> class="current"</#if>>${profilesLabel}</a>
                                <a href="${servePath}/settings/avatar"<#if 'avatar' == type> class="current"</#if>>${avatarLabel}</a>
                                <a href="${servePath}/settings/account"<#if 'account' == type> class="current"</#if>>${accountLabel}</a>
                                <a href="${servePath}/settings/invite"<#if 'invite' == type> class="current"</#if>>
                                ${inviteLabel}
                                <#if invitedUserCnt < 1><span class="todo tooltipped tooltipped-w" aria-label="${todoInviteLabel}"></span></#if>
                                </a>
                                <a href="${servePath}/settings/function"<#if 'function' == type> class="current"</#if>>${functionLabel}</a>
                                <a href="${servePath}/settings/point"<#if 'point' == type> class="current"</#if>>${pointLabel}</a>
                                <a href="${servePath}/settings/privacy"<#if 'privacy' == type> class="current"</#if>>${privacyLabel}</a>
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
            Label.confirmPwdErrorLabel = "${confirmPwdErrorLabel}";
            Label.invalidUserNicknameLabel = "${invalidUserNicknameLabel}";
            Label.previewLabel = "${previewLabel}";
            Label.unPreviewLabel = "${unPreviewLabel}";
        </script>
    </body>
</html>
</#macro>
