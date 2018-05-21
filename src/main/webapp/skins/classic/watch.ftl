<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "common/title-icon.ftl">
<!DOCTYPE html>
<html lang="zh-cmn-Hans">
<head>
<@head title="${followLabel} - ${symphonyLabel}">
    <meta name="description" content="${symDescriptionLabel}"/>
</@head>
    <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
</head>
<body>
<#include "header.ftl">
<div class="main">
    <div class="wrapper">
        <div class="content fn-clear" id="watch-pjax-container">
            <#if pjax><!---- pjax {#watch-pjax-container} start ----></#if>
            <div class="module">
                <div class="module-header fn-clear">
                    <span class="fn-right ft-fade">
                        <a pjax-title="${followingTagsLabel} - ${symphonyLabel}"
                           class="<#if "" == current>ft-gray</#if>" href="${servePath}/watch">${followingTagsLabel}</a>
                        /
                        <a pjax-title="${followingUsersLabel} - ${symphonyLabel}"
                           class="<#if "/users" == current>ft-gray</#if>"
                           href="${servePath}/watch/users">${followingUsersLabel}</a>
                        /
                        <a pjax-title="${followingUsersLabel} - ${symphonyLabel}"
                           class="<#if "/bm" == current>ft-gray</#if>"
                           href="${servePath}/watch/bm">${followingUsersLabel}</a>
                    </span>
                </div>
            <@list listData=watchingArticles/>
            </div><#if pjax><!---- pjax {#watch-pjax-container} end ----></#if>
        </div>
        <div class="side">
        <#include "side.ftl">
        </div>
    </div>
</div>
<#include "common/domains.ftl">
<#include "footer.ftl">
<@listScript/>
<script>
    $.pjax({
        selector: 'a',
        container: '#watch-pjax-container',
        show: '',
        cache: false,
        storage: true,
        titleSuffix: '',
        filter: function (href) {
            return 0 > href.indexOf('${servePath}/watch')
        },
        callback: function () {
            Util.lazyLoadCSSImage()
        },
    })
    NProgress.configure({showSpinner: false})
    $('#watch-pjax-container').bind('pjax.start', function () {
        NProgress.start()
    })
    $('#watch-pjax-container').bind('pjax.end', function () {
        NProgress.done()
    })
</script>
</body>
</html>
