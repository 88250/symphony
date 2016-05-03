<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "articles">
<div class="list content admin">
    <form method="GET" action="articles" class="form">
        <input name="id" type="text" placeholder="${articleLabel} Id"/>
        <button type="submit" class="green">${searchLabel}</button>
        <#if esEnabled || algoliaEnabled>
        <button type="button" class="btn red" onclick="searchIndex();">${searchIndexLabel}</button>
        </#if>
        <button type="button" class="btn red" onclick="window.location='/admin/add-article'">${addArticleLabel}</button>
    </form>
    <br/>
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
                <a href="/admin/article/${item.oId}" class="fn-right icon-edit" title="${editLabel}"></a>  
            </div>
            <div class="fn-clear">
                <div class="avatar" style="background-image:url('${item.articleAuthorThumbnailURL}-64.jpg?${item.articleAuthor.userUpdateTime?c}')"></div>${item.articleAuthorName} &nbsp;
                <span class="icon-tags" title="${tagLabel}"></span>
                <span class="tags">
                    ${item.articleTags}
                </span> 
                <span class="fn-right ft-gray">
                    <span class="icon-view" title="${viewCountLabel}"></span>
                    ${item.articleViewCount} &nbsp;
                    <span class="icon-cmts" title="${commentCountLabel}"></span>
                    ${item.articleCommentCount} &nbsp;
                    <span class="icon-date" title="${createTimeLabel}"></span>
                    ${item.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/articles"/>
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
