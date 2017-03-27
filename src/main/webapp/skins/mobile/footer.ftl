<div class="footer">
    <div class="wrapper ft-smaller">
        <div class="footer-nav">
            <span class="ft-fade">&COPY; ${year}</span>
            <a rel="copyright" href="https://hacpai.com" target="_blank">hacpai.com</a>
            ${visionLabel}
        </div>
        <div class="fn-hr5"></div>
        <div class="ft-fade">
            ${sloganLabel}
        </div>
        <div class="fn-hr5"></div>
        <div>
            <span class="ft-fade">Powered by <a href="http://b3log.org" class="ft-gray" target="_blank">B3log 开源</a> • 
                <a href="https://github.com/b3log/symphony" class="ft-gray" target="_blank">Sym</a>
                ${version} • ${elapsed?c}ms</span>
        </div>
    </div>
</div>
<script src="${staticServePath}/js/lib/compress/libs.min.js?${staticResourceVersion}"></script>
<script src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    var Label = {
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
        checkIcon: '${checkIcon}'<#if isLoggedIn>,
        currentUserName: '${currentUser.userName}'</#if>
    };
    Util.init(${isLoggedIn?c});
    
    <#if isLoggedIn>
    // Init [User] channel
    Util.initUserChannel("${wsScheme}://${serverHost}:${serverPort}${contextPath}/user-channel");
    </#if>
</script>
<#if algoliaEnabled>
<script src="${staticServePath}/js/lib/algolia/algolia.min.js"></script>
<script>
    Util.initSearch('${algoliaAppId}', '${algoliaSearchKey}', '${algoliaIndex}');
</script>
</#if>
