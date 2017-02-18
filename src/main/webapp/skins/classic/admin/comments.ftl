<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="content admin">
    <div class="module list">
        <ul>
            <#list comments as item>
            <li>
                <div class="fn-flex">
                    <div class="avatar tooltipped tooltipped-w" style="background-image:url('${item.commentAuthorThumbnailURL}')"
                         aria-label="${item.commentAuthorName}"></div>
                    <div class="fn-flex-1">
                        <h2>
                            <a href="${servePath}${item.commentSharpURL}">${item.commentArticleTitle}</a>
                            <span class="ft-smaller ft-gray">
                            <#if item.commentStatus == 0>${validLabel}<#else>
                            <font class="ft-red">${banLabel}</font>
                            </#if> • ${item.commentCreateTime?string('yyyy-MM-dd HH:mm')}
                            </span>
                        </h2>

                        <div class="content-reset">
                            ${item.commentContent}
                        </div>
                    </div>
                    <a href="${servePath}/admin/comment/${item.oId}" class="fn-right tooltipped tooltipped-e ft-a-title" aria-label="${editLabel}"><span class="icon-edit"></span></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/comments"/>
    </div>
</div>
</@admin>