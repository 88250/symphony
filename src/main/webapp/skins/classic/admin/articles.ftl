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
<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "articles">
<div class="content admin">
    <div class="module list">
        <form method="GET" action="${servePath}/admin/articles" class="form">
            <input name="id" type="text" placeholder="${articleLabel} Id"/>
            <button type="submit" class="green">${searchLabel}</button>
            <#if (esEnabled || algoliaEnabled) && permissions["articleReindexArticles"].permissionGrant>
                &nbsp;
            <button type="button" class="btn red" onclick="searchIndex();">${searchIndexLabel}</button>
            </#if>
            <#if permissions["articleAddArticle"].permissionGrant>  &nbsp;
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-article'">${addArticleLabel}</button>
            </#if>
        </form>
        <ul>
            <#list articles as item>
            <li>
                <div class="fn-flex">
                    <div class="avatar tooltipped tooltipped-s" aria-label="${item.articleAuthorName}"
                         style="background-image:url('${item.articleAuthorThumbnailURL20}')"></div>
                    <div class="fn-flex-1">
                        <h2>
                            <a href="${servePath}${item.articlePermalink}">${item.articleTitle}</a>
                            <span class="ft-smaller">
                            <#if item.articleStatus == 0>
                                <span class="ft-gray">${validLabel}</span>
                                <#else>
                                <font class="ft-red">${banLabel}</font>
                            </#if>
                            <#if 0 < item.articleStick>
                            <#if 9223372036854775807 <= item.articleStick><font class="ft-green">${adminLabel}</font></#if><font class="ft-green">${stickLabel}</font>
                            </#if>
                            </span>
                        </h2>
                        <span class="ft-fade ft-smaller">
                        ${item.articleTags}  • ${item.articleCreateTime?string('yyyy-MM-dd HH:mm')} •
                        ${viewCountLabel} ${item.articleViewCount} •
                        ${commentCountLabel} ${item.articleCommentCount}
                        </span>
                    </div>
                    <a href="${servePath}/admin/article/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><svg><use xlink:href="#edit"></use></svg></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/articles"/>
    </div>
</div>
<script>
    function searchIndex() {
        $.ajax({
            url: "/admin/search/index",
            type: "POST",
            cache: false,
            success: function (result, textStatus) {
                window.location.reload();
            }
        });
    }
</script>
</@admin>