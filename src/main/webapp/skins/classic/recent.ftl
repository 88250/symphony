<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${latestLabel} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear" id="recent-pjax-container">
                    <#if pjax><!---- pjax {#recent-pjax-container} start ----></#if><#if 0 < stickArticles?size>
                    <div class="module">
                        <div class="article-list list">
                            <ul class="stick">
                                <#list stickArticles as article>
                                    <#include "common/list-item.ftl">
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#if>
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right ft-fade">
                                <a pjax-title="${latestLabel} - ${symphonyLabel}" class="<#if "" == current>ft-gray</#if>" href="${servePath}/recent">${defaultLabel}</a>
                                /
                                <a pjax-title="${latestLabel} - ${symphonyLabel}"  class="<#if "/hot" == current>ft-gray</#if>" href="${servePath}/recent/hot">${hotArticlesLabel}</a>
                                /
                                <a pjax-title="${goodCmtsLabel} - ${symphonyLabel}"  class="<#if "/good" == current>ft-gray</#if>" href="${servePath}/recent/good"><span class="icon-thumbs-up"></span> ${goodCmtsLabel}</a>
                                /
                                <a pjax-title="${recentCommentLabel} - ${symphonyLabel}"  class="<#if "/reply" == current>ft-gray</#if>" href="${servePath}/recent/reply">${recentCommentLabel}</a>
                            </span>
                        </div>
                        <@list listData=latestArticles/>
                        <@pagination url="${servePath}/recent${current}" pjaxTitle="${latestLabel} - ${symphonyLabel}"/>
                    </div><#if pjax><!---- pjax {#recent-pjax-container} end ----></#if>

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
                container: '#recent-pjax-container',
                show: '',
                cache: false,
                storage: true,
                titleSuffix: '',
                filter: function(href){
                    return 0 > href.indexOf('${servePath}/recent');
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
