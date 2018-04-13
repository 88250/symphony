<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html lang="zh-cmn-Hans">
    <head>
        <@head title="${followLabel} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="content fn-clear" id="recent-pjax-container">
                <#if pjax><!---- pjax {#recent-pjax-container} start ----></#if>
                    <div class="domains fn-flex">
                        <a pjax-title="${followingTagsLabel} - ${symphonyLabel}"  class="<#if "" == current>selected</#if>" href="${servePath}/watch">${followingTagsLabel}</a>
                        <a pjax-title="${followingUsersLabel} - ${symphonyLabel}" class="<#if "/users" == current>selected</#if>" href="${servePath}/watch/users">${followingUsersLabel}</a>
                    </div>
                <@list listData=watchingArticles/>
                <#if pjax><!---- pjax {#recent-pjax-container} end ----></#if>
            </div>
            <#if domains?size != 0>
                <div class="module">
                    <div class="module-header">
                        <h2>${domainNavLabel}</h2>
                        <a href="${servePath}/domains" class="ft-gray fn-right">All Domains</a>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list domain">
                            <#list domains as domain>
                                <#if domain.domainTags?size gt 0>
                                    <li>
                                        <a rel="nofollow" class="slogan" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                                        <div class="title">
                                            <#list domain.domainTags as tag>
                                                <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                                            </#list>
                                        </div>
                                    </li>
                                </#if>
                            </#list>
                        </ul>
                    </div>
                </div>
            </#if>
            <#include "side.ftl">
        </div>
        <#include "footer.ftl">
        <@listScript/>
        <script>
            $.pjax({
                selector: 'a',
                container: '#recent-pjax-container',
                show: '',
                cache: false,
                storage: true,
                titleSuffix: '',
                filter: function(href){
                    return 0 > href.indexOf('${servePath}/recent');
                },
                callback: function () {
                    Util.lazyLoadCSSImage();
                }
            });
            NProgress.configure({ showSpinner: false });
            $('#recent-pjax-container').bind('pjax.start', function(){
                NProgress.start();
            });
            $('#recent-pjax-container').bind('pjax.end', function(){
                NProgress.done();
            });
        </script>
    </body>
</html>
