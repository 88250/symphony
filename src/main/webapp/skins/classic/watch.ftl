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
                           class="<#if "/breezemoons" == current>ft-gray</#if>"
                           href="${servePath}/watch/breezemoons">${breezemoonLabel}</a>
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
<script src="${staticServePath}/js/breezemoon${miniPostfix}.js?${staticResourceVersion}"></script>
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
            Breezemoon.init()
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
