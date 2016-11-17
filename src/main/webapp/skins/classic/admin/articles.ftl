<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "articles">
<div class="content admin">
    <div class="module list">
        <form method="GET" action="${servePath}/admin/articles" class="form">
            <input name="id" type="text" placeholder="${articleLabel} Id"/>
            <button type="submit" class="green">${searchLabel}</button>
            <#if esEnabled || algoliaEnabled>
            <button type="button" class="btn red" onclick="searchIndex();">${searchIndexLabel}</button>
            </#if>
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-article'">${addArticleLabel}</button>
        </form>
        <ul>
            <#list articles as item>
            <li>
                <div class="fn-clear first">
                    <a href="${item.articlePermalink}">${item.articleTitle}</a> &nbsp;
                    <#if item.articleStatus == 0>
                    <span class="ft-gray">${validLabel}</span>
                    <#else>
                    <font class="ft-red">${banLabel}</font>
                    </#if>
                    <#if 0 < item.articleStick>
                    <#if 9223372036854775807 <= item.articleStick><font class="ft-green">${adminLabel}</font></#if><font class="ft-green">${stickLabel}</font>
                    </#if>
                    <a href="${servePath}/admin/article/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><span class="icon-edit"></span></a>
                </div>
                <div class="fn-clear">
                    <div class="avatar" style="background-image:url('${item.articleAuthorThumbnailURL20}')"></div>${item.articleAuthorName} &nbsp;
                    <span class="tooltipped tooltipped-w" aria-label="${tagLabel}"><span class="icon-tags"></span></span>
                    <span class="tags">
                        ${item.articleTags}
                    </span> 
                    <span class="fn-right ft-gray">
                        <span class="tooltipped tooltipped-n" aria-label="${viewCountLabel}"><span class="icon-view"></span></span>
                        ${item.articleViewCount} &nbsp;
                        <span class="tooltipped tooltipped-n" aria-label="${commentCountLabel}"><span class="icon-cmts"></span></span>
                        ${item.articleCommentCount} &nbsp;
                        <span class="tooltipped tooltipped-n" aria-label="${createTimeLabel}"><span class="icon-date"></span></span>
                        ${item.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                    </span>
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
