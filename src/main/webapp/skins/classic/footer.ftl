<div class="footer">
    <div class="wrapper">
        <div class="fn-flex-1">
            <div class="footer-nav fn-clear">
                <a rel="help" href="https://hacpai.com/article/1440573175609">${aboutLabel}</a>
                <a href="https://hacpai.com/article/1457158841475">API</a>
                <a href="${servePath}/tag/系统公告">${symAnnouncementLabel}</a>
                <a href="${servePath}/tag/Q%26A">${qnaLabel}</a>
                <a href="${servePath}/domains">${domainLabel}</a>
                <a href="${servePath}/tags">${tagLabel}</a>
                <a href="https://hacpai.com/article/1460083956075">${adDeliveryLabel}</a>
                <a href="${servePath}/statistic" class="last">${dataStatLabel}</a>

                <div class="fn-right">
                    <span class="ft-gray">&COPY; ${year}</span>
                    <a rel="copyright" href="https://hacpai.com" target="_blank">hacpai.com</a>
                    ${visionLabel}</div>
            </div>
            <div class="fn-clear ft-smaller ft-fade">
                       ${sloganLabel}
                    <div class="fn-right">
                       Powered by <a href="http://b3log.org" class="ft-gray" target="_blank">B3log 开源</a> • 
                            <a href="https://github.com/b3log/symphony" class="ft-gray" target="_blank">Sym</a>
                            ${version} • ${elapsed?c}ms
                    </div>
                </div>
        </div>
    </div>
</div>
<div class="icon-up" onclick="Util.goTop()"></div>
<script type="text/javascript" src="${staticServePath}/js/lib/compress/libs.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
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
        isLoggedIn: ${isLoggedIn?c}
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
