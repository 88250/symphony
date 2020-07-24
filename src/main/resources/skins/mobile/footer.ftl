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
<div class="footer">
    <div class="wrapper ft-smaller">
        <div class="footer-nav">
            ${visionLabel}
        </div>
        <div class="fn-hr5"></div>
        <div>
            ${sloganLabel}
        </div>
        <div class="fn-hr5"></div>
        <div>
            © 2012-present <a href="https://b3log.org" target="_blank">B3log 开源</a>
            <div class="fn-hr5"></div>
            <a href="https://sym.b3log.org" target="_blank">Sym</a>
            ${version}
        </div>
        <#if footerBeiAnHao != ''>
            <div class="fn-hr5"></div>
            <div>
                <a href="http://www.miitbeian.gov.cn/" target="_blank">${footerBeiAnHao}</a>
            </div>
        </#if>
    </div>
</div>
<script src="${staticServePath}/js/symbol-defs${miniPostfix}.js?${staticResourceVersion}"></script>
<script src="${staticServePath}/js/lib/compress/libs.min.js?${staticResourceVersion}"></script>
<script src="https://cdn.jsdelivr.net/npm/vditor@3.3.8/dist/index.min.js"></script>
<script src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
  var Label = {
    commentEditorPlaceholderLabel: '${commentEditorPlaceholderLabel}',
    langLabel: '${langLabel}',
    reportSuccLabel: '${reportSuccLabel}',
    breezemoonLabel: '${breezemoonLabel}',
    confirmRemoveLabel: "${confirmRemoveLabel}",
    invalidPasswordLabel: "${invalidPasswordLabel}",
    loginNameErrorLabel: "${loginNameErrorLabel}",
    followLabel: "${followLabel}",
    unfollowLabel: "${unfollowLabel}",
    symphonyLabel: "${symphonyLabel}",
    visionLabel: "${visionLabel}",
    cmtLabel: "${cmtLabel}",
    collectLabel: "${collectLabel}",
    uncollectLabel: "${uncollectLabel}",
    desktopNotificationTemplateLabel: "${desktopNotificationTemplateLabel}",
    servePath: "${servePath}",
    staticServePath: "${staticServePath}",
    isLoggedIn: ${isLoggedIn?c},
    funNeedLoginLabel: '${funNeedLoginLabel}',
    notificationCommentedLabel: '${notificationCommentedLabel}',
    notificationReplyLabel: '${notificationReplyLabel}',
    notificationAtLabel: '${notificationAtLabel}',
    notificationFollowingLabel: '${notificationFollowingLabel}',
    pointLabel: '${pointLabel}',
    sameCityLabel: '${sameCityLabel}',
    systemLabel: '${systemLabel}',
    newFollowerLabel: '${newFollowerLabel}',
    makeAsReadLabel: '${makeAsReadLabel}',
    imgMaxSize: ${imgMaxSize?c},
    fileMaxSize: ${fileMaxSize?c},
    <#if isLoggedIn>
    currentUserName: '${currentUser.userName}',
    </#if>
    <#if csrfToken??>
    csrfToken: '${csrfToken}'
    </#if>
  }
  Util.init(${isLoggedIn?c})

  <#if isLoggedIn>
  // Init [User] channel
  Util.initUserChannel("${wsScheme}://${serverHost}:${serverPort}${contextPath}/user-channel")
  </#if>
</script>
<#if algoliaEnabled>
    <script src="${staticServePath}/js/lib/algolia/algolia.min.js"></script>
    <script>
      Util.initSearch('${algoliaAppId}', '${algoliaSearchKey}', '${algoliaIndex}')
    </script>
</#if>
