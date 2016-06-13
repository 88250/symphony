<div class="footer">
    <div class="wrapper">
            <div class="footer-nav">
                    <span class="ft-fade">&COPY; ${year}</span>
                    <a rel="copyright" href="https://hacpai.com" target="_blank">hacpai.com</a>
                    ${visionLabel}
            </div>
            <div>
                <span class="ft-fade">Powered by <a href="http://b3log.org" class="ft-gray" target="_blank">B3log 开源</a> • 
                    <a href="https://github.com/b3log/symphony" class="ft-gray" target="_blank">Sym</a>
                        ${version} • ${elapsed?c}ms</span>
            </div>
    </div>
</div>
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
