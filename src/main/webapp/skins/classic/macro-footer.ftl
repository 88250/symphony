<#macro footer>
<div class="footer">
    <div class="wrapper fn-clear">
        <div class="fn-left">
            <img src="/images/logo.png" />
        </div>
        <div class="fn-right" style="width: 800px">
            <div>
                <a href="">about</a>
                <a href="">tag1</a>
                <a href="">tag2</a>
                <a href="">blog</a>
            </div>
            <div class="fn-clear">
                <div class="fn-left">
                    自由*平等*奔放
                    <span class="ft-small">&COPY; 2012 <a href="http://www.b3log.org" target="_blank">B3LOG.ORG</a></span>
                </div>
                <div class="fn-right">
                    <span class="ft-small">当前在线人数：</span>${onlineVisitorCnt}
                    <span class="ft-small">用户数：</span>${statistic.statisticMemberCount}
                    <span class="ft-small">文章数：</span>${statistic.statisticArticleCount}
                    <span class="ft-small">tag 数：</span>${statistic.statisticTagCount}
                    <span class="ft-small">评论数：</span>${statistic.statisticCmtCount}
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript" src="/js/lib/jquery/jquery.min.js"></script>
<script type="text/javascript" src="/js/common.js"></script>
<#nested>
<script>
    Util.init();
</script>
</#macro>
