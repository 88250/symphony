<#include "macro-admin.ftl">
<@admin "articles">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" name="oId" value="${article.oId}" readonly="readonly" />

            <label for="articleAuthorEmail">${authorEmailLabel}</label>
            <input type="text" id="articleAuthorEmail" name="articleAuthorEmail" value="${article.articleAuthorEmail}" readonly="readonly" />

            <label for="articleAuthorId">${authorIdLabel}</label>
            <input type="text" id="articleAuthorId" name="articleAuthorId" value="${article.articleAuthorId}" readonly="readonly" />

            <label for="articleCommentCount">${commentCountLabel}</label>
            <input type="text" id="articleCommentCount" name="articleCommentCount" value="${article.articleCommentCount?c}" readonly="readonly" />

            <label for="articleViewCount">${viewCountLabel}</label>
            <input type="text" id="articleViewCount" name="articleViewCount" value="${article.articleViewCount?c}" readonly="readonly" />

            <label for="articlePermalink">${permalinkLabel}</label>
            <input type="text" id="articlePermalink" name="articlePermalink" value="${article.articlePermalink}" />

            <label for="articleCreateTime">${createTimeLabel}</label>
            <input type="text" id="articleCreateTime" name="articleCreateTime" value="${article.articleCreateTime?c}" />

            <label for="articleUpdateTime">${updateTimeLabel}</label>
            <input type="text" id="articleUpdateTime" name="articleUpdateTime" value="${article.articleUpdateTime?c}" />

            <label for="syncWithSymphonyClient">${symClientSyncLabel}</label>
            <input type="text" id="syncWithSymphonyClient" name="syncWithSymphonyClient" value="${article.syncWithSymphonyClient?c}" readonly="readonly" />

            <label for="clientArticleId">${clientArticleIdLabel}</label>
            <input type="text" id="clientArticleId" name="clientArticleId" value="${article.clientArticleId}" readonly="readonly" />

            <label for="articleEditorType">${eidotrTypeLabel}</label>
            <input type="text" id="articleEditorType" name="articleEditorType" value="${article.articleEditorType}" readonly="readonly" />
        </div>
    </div>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/article/${article.oId}" method="POST">
                <label for="articleTitle">${titleLabel}</label>
                <input type="text" id="articleTitle" name="articleTitle" value="${article.articleTitle}" />

                <label for="articleTags">${tagLabel}</label>
                <input type="text" id="articleTags" name="articleTags" value="${article.articleTags}" />

                <label for="articleContent">${contentLabel}</label>
                <textarea id="articleContent" name="articleContent">${article.articleContent}</textarea>

                <label for="articleRewardContent">${rewardContentLabel}</label>
                <textarea id="articleRewardContent" name="articleRewardContent">${article.articleRewardContent}</textarea>

                <label for="articleRewardPoint">${rewardPointLabel}</label>
                <input type="text" id="articleRewardPoint" name="articleRewardPoint" value="${article.articleRewardPoint}"/>

                <label for="articleCommentable">${commentableLabel}</label>
                <input type="text" id="articleCommentable" name="articleCommentable" value="${article.articleCommentable?c}" />

                <label for="articleStatus">${articleStatusLabel}</label>
                <input type="text" id="articleStatus" name="articleStatus" value="${article.articleStatus}" />

                <label for="articleGoodCnt">${goodCntLabel}</label>
                <input type="text" id="articleGoodCnt" name="articleGoodCnt" value="${article.articleGoodCnt}" />

                <label for="articleBadCnt">${badCntLabel}</label>
                <input type="text" id="articleBadCnt" name="articleBadCnt" value="${article.articleBadCnt}" />

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>