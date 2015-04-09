<#include "macro-admin.ftl">
<@admin "comments">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" value="${comment.oId}" readonly="readonly" />

            <label for="commentAuthorEmail">${userNameLabel}</label>
            <input type="text" id="commentAuthorEmail" name="commentAuthorEmail" value="${comment.commentAuthorEmail}" readonly="readonly" />

            <label for="commentOnArticleId">${articleLabel} Id</label>
            <input type="text" id="commentOnArticleId" name="commentOnArticleId" value="${comment.commentOnArticleId}" readonly="readonly" />

            <label for="commentSharpURL">URL</label>
            <input type="text" id="commentSharpURL" name="commentSharpURL" value="${comment.commentSharpURL}" readonly="readonly" />
        </div>
    </div>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/comment/${comment.oId}" method="POST">
                <label for="commentStatus">${commentStatusLabel}</label>
                <input type="text" id="commentStatus" name="commentStatus" value="${comment.commentStatus}" />

                <label for="commentContent">${commentContentLabel}</label>
                <textarea style="width: 100%" id="commentContent" name="commentContent">${comment.commentContent}</textarea>

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>