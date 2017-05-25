<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "tags">
<div class="content admin">
    <div class="module list">
        <form method="GET" action="${servePath}/admin/tags" class="form">
            <input name="title" type="text" placeholder="${tagLabel}"/>
            <button type="submit" class="green">${searchLabel}</button>  &nbsp;
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-tag'">${addTagLabel}</button>
            <button type="button" class="btn red" onclick="removeUnusedTags();">${removeUnusedTagsLabel}</button>
        </form>
        <ul>
            <#list tags as item>
            <li>
                <div class="fn-flex">
                    <#if item.tagIconPath != ''>
                        <div class="avatar" style="background-image:url('${staticServePath}/images/tags/${item.tagIconPath}')"></div>
                    </#if>
                    <div class="fn-flex-1">
                        <h2>
                            <a href="${servePath}/tag/${item.tagURI}">${item.tagTitle}</a> •
                            <span class="ft-smaller ft-gray">
                                <#if item.tagStatus == 0>
                                    <span>${validLabel}</span>
                                    <#else><font class="ft-red">${banLabel}</font>
                                </#if> •
                                ${item.tagCreateTime?string('yyyy-MM-dd HH:mm')} •
                                ${refCountLabel} ${item.tagReferenceCount} •
                                ${commentCountLabel} ${item.tagCommentCount}
                                </span>
                            </span>
                        </h2>
                        ${item.tagDescription}
                    </div>
                    <a href="${servePath}/admin/tag/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><svg><use xlink:href="#edit"></use></svg></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/tags"/>
    </div>
</div>
<script>
    function removeUnusedTags() {
        $.ajax({
            url: "/admin/tags/remove-unused",
            type: "POST",
            cache: false,
            success: function (result, textStatus) {
                window.location.reload();
            }
        });
    }
</script>
</@admin>
