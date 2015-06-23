<div class="footer">
    <div class="wrapper fn-clear">
        <div class="fn-left" style="margin-top: 4px;">
            <a href="http://b3log.org" target="_blank"><img src="http://b3log.org/images/b3log.png" alt="b3log" width="48" /></a>
            &nbsp;
            <a href="https://wide.b3log.org" target="_blank"><img src="http://b3log.org/images/wide.png" alt="wide" width="48" /></a>
        </div>
        <div class="fn-right">
            <div class="footer-nav">
                <a rel="help" href="/about">${aboutLabel}</a> 
                <a rel="help" href="/tags/B3log%20Announcement">${b3logAnnouncementLabel}</a>
                <a rel="help" href="/tags/Q%26A">${qnaLabel}</a>
                <a href="/tags" class="last">${tagLabel}</a>
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
                    ${visionLabel}
                    <span class="ft-small">&COPY; ${year} </span>
                    <a rel="copyright" href="http://b3log.org" target="_blank">B3LOG.ORG</a>
                    <span class="ft-small">${version}</span>
                    <span class="fn-none">
                        ${siteVisitStatCode}
                    </span>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="icon-up icon" onclick="Util.goTop()"></div>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.min.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/md5.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.linkify-1.0-min.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.notification-1.0.5.js?${staticResourceVersion}"></script>
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
