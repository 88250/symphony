<div class="footer">
    <div class="wrapper">
        <div class="fn-flex-1">
            <div class="footer-nav fn-clear">
                <a rel="help" href="https://hacpai.com/article/1440573175609">${aboutLabel}</a>
                <a href="https://hacpai.com/article/1457158841475">API</a>
                <a href="/tag/系统公告">${symAnnouncementLabel}</a>
                <a href="/tag/Q%26A">${qnaLabel}</a>
                <a href="/domains">${domainLabel}</a>
                <a href="/tags">${tagLabel}</a>
                <a rel="nofollow" href="/chat-room">${chatRoomLabel}</a>
                <a href="https://hacpai.com/article/1460083956075">${adDeliveryLabel}</a>
                <a href="/statistic" class="last">${dataStatLabel}</a>

                <div class="fn-right">
                    <span class="ft-gray">&COPY; ${year}</span>
                    <a rel="copyright" href="https://hacpai.com" target="_blank">hacpai.com</a>
                    ${visionLabel}</div>
            </div>
            <div class="fn-clear">
                <div class="fn-left info responsive-hide">
                    <span class="ft-gray">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt?c} &nbsp;
                    <span class="ft-gray">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount?c} &nbsp;
                    <span class="ft-gray">${memberLabel}</span> ${statistic.statisticMemberCount?c} &nbsp;
                    <span class="ft-gray">${articleLabel}</span> ${statistic.statisticArticleCount?c} &nbsp;
                    <span class="ft-gray">${domainLabel}</span> ${statistic.statisticDomainCount?c} &nbsp;
                    <span class="ft-gray">${tagLabel}</span> ${statistic.statisticTagCount?c} &nbsp;
                    <span class="ft-gray">${cmtLabel}</span> ${statistic.statisticCmtCount?c}
                </div>
                <div class="fn-right">
                    <span class="ft-gray">Powered by <a href="http://b3log.org" target="_blank">B3log 开源</a> • 
                        <a href="https://github.com/b3log/symphony" target="_blank">Sym</a>
                        ${version} • ${elapsed?c}ms</span>
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
        staticServePath: "${staticServePath}"
    };
    Util.init(${isLoggedIn?c});
</script>
<#if algoliaEnabled>
<script src="${staticServePath}/js/lib/algolia/algolia.min.js"></script>
<script>
    Util.initSearch('${algoliaAppId}', '${algoliaSearchKey}', '${algoliaIndex}');
</script>
</#if>
