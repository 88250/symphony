<div class="footer">
    <div class="wrapper">
        <div class="fn-flex-1">
            <div class="footer-nav fn-clear">
                <a rel="help" href="/about">${aboutLabel}</a>
                <a href="/timeline">${timelineLabel}</a>
                <a href="/tags/系统公告">${symAnnouncementLabel}</a>
                <a href="/tags/Q%26A">${qnaLabel}</a>
                <a href="/tags" class="last">${tagLabel}</a>

                <span class="fn-right">${visionLabel}</span>
            </div>
            <div class="fn-clear">
                <div class="fn-left info">
                    <span class="ft-small">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt?c} &nbsp;
                    <span class="ft-small">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount?c} &nbsp;
                    <span class="ft-small">${memberLabel}</span> ${statistic.statisticMemberCount?c} &nbsp;
                    <span class="ft-small">${articleLabel}</span> ${statistic.statisticArticleCount?c} &nbsp;
                    <span class="ft-small">${tagLabel}</span> ${statistic.statisticTagCount?c} &nbsp;
                    <span class="ft-small">${cmtLabel}</span> ${statistic.statisticCmtCount?c}
                </div>
                <div class="fn-right">
                    <span class="ft-small">&COPY; ${year} </span>
                    <a rel="copyright" href="http://hacpai.com" target="_blank">hacpai.com</a>
                    <span class="ft-small">${version} · ${elapsed?c}ms</span>
                    <span class="fn-none">
                        ${siteVisitStatCode}
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="icon-up icon" onclick="Util.goTop()"></div>
<script>var isLoggedIn = ${isLoggedIn?c};</script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/md5.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.linkify-1.0-min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.notification-1.0.5.js"></script>
<script>
    Util.init();
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
        desktopNotificationTemplateLabel: "${desktopNotificationTemplateLabel}"
    };
</script>
