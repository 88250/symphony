<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "articles">
<div class="admin">
    <div class="list">
        <form method="GET" action="${servePath}/admin/articles" class="form wrapper">
            <input name="id" type="text" placeholder="${articleLabel} Id"/>
            <button type="submit" class="green">${searchLabel}</button> <br><br>
            <#if (esEnabled || algoliaEnabled) && permissions["articleReindexArticle"].permissionGrant>
            <button type="button" class="btn red" onclick="searchIndex();">${searchIndexLabel}</button> &nbsp;
            </#if>
            <#if permissions["articleAddArticle"].permissionGrant>
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
                    <a href="${servePath}/admin/article/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><span class="icon-edit"></span></a>
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
