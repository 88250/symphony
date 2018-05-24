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
<!DOCTYPE html>
<html lang="zh-cmn-Hans">
<head>
<@head title="${breezemoonLabel} - ${symphonyLabel}">
    <meta name="description" content="只与清风、明月为伴。清凉的风，明朗的月。"/>
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
                <#if permissions["commonAddBreezemoon"].permissionGrant>
                <div class="list">
                    <ul class="form">
                        <li>
                            <input  style="padding-right: 89px;" id="breezemoonInput" type="text">
                            <button onclick="Breezemoon.add()" id="breezemoonBtn" data-csrftoken="${csrfToken}"
                                    class="absolute">${breezemoonLabel}</button>
                        </li>
                    </ul>
                </div>
                </#if>
                <div class="list">
                    <ul id="breezemoonList">
                        <#list watchingBreezemoons as item>
                            <li class="fn-flex" id="${item.oId}">
                                <a class="tooltipped tooltipped-n avatar"
                                   style="background-image:url('${item.breezemoonAuthorThumbnailURL48}')"
                                   rel="nofollow" href="${servePath}/member/${item.breezemoonAuthorName}"
                                   aria-label="Vanessa">
                                </a>
                                <div class="fn-flex-1">
                                    <div class="ft-fade">
                                        <a href="${servePath}/member/${item.breezemoonAuthorName}">${item.breezemoonAuthorName}</a>
                                        •
                                        <span class="ft-smaller">
                                            ${item.timeAgo}
                                        </span>
                                        <span class="ft-smaller ua" data-ua="${item.breezemoonUA}"></span>

                                        <div class="fn-right">
                                            <#if isLoggedIn && permissions["commonRemoveBreezemoon"].permissionGrant && item.breezemoonAuthorName == currentUser.userName>
                                            <span class="tooltipped tooltipped-n ft-red rm" aria-label="${removeLabel}">
                                                <svg><use xlink:href="#remove"></use></svg>
                                            </span>
                                            </#if>
                                            <#if isLoggedIn && permissions["commonUpdateBreezemoon"].permissionGrant && item.breezemoonAuthorName == currentUser.userName>
                                            &nbsp;&nbsp;
                                            <span class="tooltipped tooltipped-n ft-a-title edit"
                                                  aria-label="${editLabel}">
                                                <svg><use xlink:href="#edit"></use></svg>
                                            </span>
                                            &nbsp;&nbsp;
                                            </#if>
                                            <#--
                                            <span class="tooltipped tooltipped-n ft-a-title copy"
                                                  aria-label="${copyLabel}">
                                                <svg><use xlink:href="#articles"></use></svg>
                                            </span>
                                            -->
                                            <textarea style="position: fixed;left: -10000px;">${servePath}/watch/breezemoons#${item.oId}</textarea>
                                        </div>
                                    </div>
                                    <div class="content-reset">${item.breezemoonContent}</div>
                                </div>
                            </li>
                        </#list>
                    </ul>
                </div>
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
