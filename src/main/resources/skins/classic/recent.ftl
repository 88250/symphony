<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<#include "common/title-icon.ftl">
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
                                <a pjax-title="${goodCmtsLabel} - ${symphonyLabel}"  class="<#if "/good" == current>ft-gray</#if>" href="${servePath}/recent/good"><svg class="fn-text-top"><use xlink:href="#thumbs-up"></use></svg> ${goodCmtsLabel}</a>
                                /
                                <a pjax-title="${recentCommentLabel} - ${symphonyLabel}"  class="<#if "/reply" == current>ft-gray</#if>" href="${servePath}/recent/reply">${recentCommentLabel}</a>
                            <a class="recent-rss" href="${servePath}/rss/recent.xml">
                              <svg><use xlink:href="#iconRss"></use></svg>
                            </a>
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
                },
                callback: function () {
                    Util.parseMarkdown();
                    Util.parseHljs()
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
