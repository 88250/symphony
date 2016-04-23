<div class="footer">
    <div class="wrapper">
            <div class="footer-nav">
                <a class="ft-gray" rel="help" href="https://hacpai.com/article/1440573175609">${aboutLabel}</a>
                <a class="ft-gray" href="https://hacpai.com/article/1457158841475">API</a>
                <a class="ft-gray" href="/tag/系统公告">${symAnnouncementLabel}</a>
                <a class="ft-gray" href="/tag/Q%26A">${qnaLabel}</a>
                <a class="ft-gray" href="/domains">${domainLabel}</a>
                <a class="ft-gray" href="/tags">${tagLabel}</a>
                <a class="ft-gray" rel="nofollow" href="/cr">${chatRoomLabel}</a>
                <a class="ft-gray last" href="https://hacpai.com/article/1460083956075">${adDeliveryLabel}</a>

                <div>
                    <span class="ft-fade">&COPY; ${year}</span>
                    <a rel="copyright" href="https://hacpai.com" target="_blank">hacpai.com</a>
                    ${visionLabel}</div>
            </div>
            <div>
                <span class="ft-fade">Powered by <a href="http://b3log.org" class="ft-gray" target="_blank">B3log 开源</a> • 
                    <a href="https://github.com/b3log/symphony" class="ft-gray" target="_blank">Sym</a>
                        ${version} • ${elapsed?c}ms</span>
            </div>
    </div>
</div>
<script>var isLoggedIn = ${isLoggedIn?c};</script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/md5.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.linkify-1.0-min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.notification-1.0.5.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/ua-parser.min.js"></script>
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
    Util.init();
</script>
<#if algoliaEnabled>
<script src="${staticServePath}/js/lib/algolia/algoliasearch.min.js"></script>
<script src="${staticServePath}/js/lib/algolia/autocomplete.jquery.min.js"></script>
<script>
    Util.initSearch('${algoliaAppId}', '${algoliaSearchKey}', '${algoliaIndex}');
</script>
</#if>
