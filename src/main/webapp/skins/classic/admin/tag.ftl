<#include "macro-admin.ftl">
<@admin "tags">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" value="${tag.oId}" readonly="readonly" />

            <label for="tagReferenceCount">${refCountLabel}</label>
            <input type="text" id="tagReferenceCount" name="tagReferenceCount" value="${tag.tagReferenceCount}" readonly="readonly" />

            <label for="tagCommentCount">${commentCountLabel}</label>
            <input type="text" id="tagCommentCount" name="tagCommentCount" value="${tag.tagCommentCount}" readonly="readonly" />
        </div>
    </div>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/tag/${tag.oId}" method="POST">
                <label for="tagTitle">${tagLabel}${updateCaseOnlyLabel}</label>
                <input type="text" id="tagTitle" name="tagTitle" value="${tag.tagTitle}" />

                <label for="tagDescription">${descriptionLabel}</label>
                <textarea style="width: 100%" rows="5" id="tagDescription" name="tagDescription">${tag.tagDescription}</textarea>

                <label for="tagIconPath">${iconPathLabel}</label>
                <input type="text" id="tagIconPath" name="tagIconPath" value="${tag.tagIconPath}" />

                <label for="tagStatus">${tagStatusLabel}</label>
                <input type="text" id="tagStatus" name="tagStatus" value="${tag.tagStatus}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>