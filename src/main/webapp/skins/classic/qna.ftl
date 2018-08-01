<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

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
    <@head title="${qnaLabel} - ${symphonyLabel}">
    <meta name="description" content="${symDescriptionLabel}"/>
    </@head>
    <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
</head>
<body>
<#include "header.ftl">
<div class="main">
    <div class="wrapper">
        <div class="content fn-clear" id="qna-pjax-container">
            <#if pjax><!---- pjax {#qna-pjax-container} start ----></#if>
            <div class="module">
                <div class="module-header fn-clear">
                    <span class="fn-right ft-fade">
                       <a pjax-title="${qnaLabel} - ${symphonyLabel}"
                          class="<#if "" == current>ft-gray</#if>"
                          href="${servePath}/qna">${defaultLabel}</a>
                        /
                        <a pjax-title="${qnaLabel} - ${symphonyLabel}"
                           class="<#if "/unanswered" == current>ft-gray</#if>"
                           href="${servePath}/qna/unanswered">${unansweredLabel}</a>
                        /
                        <a pjax-title="${qnaLabel} - ${symphonyLabel}"
                           class="<#if "/reward" == current>ft-gray</#if>"
                           href="${servePath}/qna/reward">
                        ${highRewardLabel}</a>
                        /
                        <a pjax-title="${qnaLabel} - ${symphonyLabel}"
                           class="<#if "/hot" == current>ft-gray</#if>"
                           href="${servePath}/qna/hot">
                            <svg class="fn-text-top">
                                <use xlink:href="#thumbs-up"></use>
                            </svg> ${hotArticlesLabel}</a>
                        &nbsp;
                        <a class="ft-red" href="${servePath}/post?type=5">
                        ${IHaveAQuestionLabel}</a>
                    </span>
                </div>
                <@list listData=latestArticles/>
                <@pagination url="${servePath}/qna${current}" pjaxTitle="${latestLabel} - ${symphonyLabel}"/>
            </div><#if pjax><!---- pjax {#qna-pjax-container} end ----></#if>
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
        container: '#qna-pjax-container',
        show: '',
        cache: false,
        storage: true,
        titleSuffix: '',
        filter: function (href) {
            return 0 > href.indexOf('${servePath}/qna')
        },
        callback: function () {
            Util.parseMarkdown()
        },
    })
    NProgress.configure({showSpinner: false})
    $('#qna-pjax-container').bind('pjax.start', function () {
        NProgress.start()
    })
    $('#qna-pjax-container').bind('pjax.end', function () {
        NProgress.done()
    })
</script>
</body>
</html>
