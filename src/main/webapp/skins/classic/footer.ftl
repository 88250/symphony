<div class="footer">
    <div class="wrapper fn-clear">
        <div class="fn-left">
            <img src="/images/logo.png" />
        </div>
        <div class="fn-right" style="width: 800px">
            <div>
                <a rel="help" href="/about">About</a>
                <a rel="help" href="/tags/B3log%20Announcement">${b3logAnnouncementLabel}</a>
                <a rel="help" href="/tags/Q&A">Q&amp;A</a>
                <a rel="tag" href="/tags/Java">Java</a>
                <a href="/tags">Tags</a>
            </div>
            <div class="fn-clear">
                <div class="fn-left">
                    ${visionLabel}
                    <span class="ft-small">&COPY; 2012 <a rel="copyright" href="http://www.b3log.org" target="_blank">B3LOG.ORG</a></span>
                </div>
                <div class="fn-right info">
                    <span class="ft-small">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt}
                    <span class="ft-small">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount}
                    <span class="ft-small">${memberCountLabel}</span> ${statistic.statisticMemberCount}
                    <span class="ft-small">${articleCountLabel}</span> ${statistic.statisticArticleCount}
                    <span class="ft-small">${tagCountLabel}</span> ${statistic.statisticTagCount}
                    <span class="ft-small">${cmtCountLabel}</span> ${statistic.statisticCmtCount}
                    <span class="ft-small">${versionLabel}</span> ${version}
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript" src="/js/lib/jquery/jquery.min.js"></script>
<script type="text/javascript" src="/js/common.js"></script>
<script>
    Util.init();
    Label.invalidPasswordLabel = "${invalidPasswordLabel}";
    Label.loginNameErrorLabel = "${loginNameErrorLabel}";
</script>
