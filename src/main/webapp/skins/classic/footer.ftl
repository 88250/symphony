<div class="footer">
    <div class="wrapper fn-clear">
        <div class="fn-left">
            <img src="${staticServePath}/images/logo.png" />
        </div>
        <div class="fn-right" style="width: 800px">
            <div class="footer-nav">
                <a rel="help" href="/about">${aboutLabel}</a> 
                <a rel="help" href="/tags/B3log%20Announcement">${b3logAnnouncementLabel}</a>
                <a rel="help" href="/tags/Q&A">${qnaLabel}</a>
                <a rel="tag" href="/tags/Java">Java</a>
                <a href="/tags" class="last">${tagLabel}</a>
            </div>
            <div class="fn-clear">
                <div class="fn-left info">
                    <span class="ft-small">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt} &nbsp;
                    <span class="ft-small">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount} &nbsp;
                    <span class="ft-small">${memberLabel}</span> ${statistic.statisticMemberCount} &nbsp;
                    <span class="ft-small">${articleLabel}</span> ${statistic.statisticArticleCount} &nbsp;
                    <span class="ft-small">${tagLabel}</span> ${statistic.statisticTagCount} &nbsp;
                    <span class="ft-small">${cmtLabel}</span> ${statistic.statisticCmtCount}
                </div>
                <div class="fn-right">
                    ${visionLabel}
                    <span class="ft-small">&COPY; ${year} </span>
                    <a rel="copyright" href="http://www.b3log.org" target="_blank">B3LOG.ORG</a>
                    <span class="ft-small">${version}</span>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="go-top" onclick="Util.goTop()"></div>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.min.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/common.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/md5.js?${staticResourceVersion}"></script>
<script>
    Util.init();
    Label.invalidPasswordLabel = "${invalidPasswordLabel}";
    Label.loginNameErrorLabel = "${loginNameErrorLabel}";
</script>
