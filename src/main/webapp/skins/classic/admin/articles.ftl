<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "articles">
<div class="list content admin">
    <ul>
        <#list articles as item>
        <li onclick="window.location = '/admin/article/${item.oId}'">
            <a href="${item.articlePermalink}">${item.articleTitle}</a> &nbsp;
            ${articleStatusLabel}
            <a href="/admin/article/${item.oId}" class="fn-right" title="${editLabel}">
                <span class="icon icon-edit"></span>
            </a>
            <br/>
            <img class="avatar"
                 title="TODO" 
                 src="http://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140&amp;d=http://symphony.b3log.org/images/user-thumbnail.png">
             Vanessa &nbsp;
            <span class="icon icon-tags" title="${tagLabel}"></span>
            111, 222  
            <span class="fn-right ft-small">
                <span class="icon icon-view" title="${viewCountLabel}"></span>
                ${item.articleViewCount} &nbsp;
                <span class="icon icon-cmts" title="${commentCountLabel}"></span>
                ${item.articleCommentCount} &nbsp;
                <span class="icon icon-date" title="${createTimeLabel}"></span>
                ${item.articleCreateTime?string('yyyy-MM-dd HH:mm')}
            </span>

        </li>
        </#list>
    </ul>
    <@pagination url="/admin/articles"/>
</div>
</@admin>
