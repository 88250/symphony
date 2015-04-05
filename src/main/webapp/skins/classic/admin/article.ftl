<#include "macro-admin.ftl">
<@admin "articles">
<div class="content">
    <form action="/admin/article/${article.oId}" method="POST">
        <!-- The following items are unmodifiable -->
        <div class="form-item">
            <label class="form-label" for="oId">Id</label>
            <input class="form-input" id="oId" name="oId" value="${article.oId}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="articleTags">${tagLabel}</label>
            <input class="form-input"  id="articleTags" name="articleTags" value="${article.articleTags}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="articleAuthorEmail">${authorEmailLabel}</label>
            <input class="form-input"  id="articleAuthorEmail" name="articleAuthorEmail" value="${article.articleAuthorEmail}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="articleAuthorId">${authorIdLabel}</label>
            <input class="form-input"  id="articleAuthorId" name="articleAuthorId" value="${article.articleAuthorId}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="articleCommentCount">${commentCountLabel}</label>
            <input class="form-input"  id="articleCommentCount" name="articleCommentCount" value="${article.articleCommentCount}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="articleViewCount">${viewCountLabel}</label>
            <input class="form-input"  id="articleViewCount" name="articleViewCount" value="${article.articleViewCount}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="articlePermalink">${permalinkLabel}</label>
            <input class="form-input"  id="articlePermalink" name="articlePermalink" value="${article.articlePermalink}" />
        </div>

        <div>
            <label class="form-label" for="articleCreateTime">${createTimeLabel}</label>
            <input class="form-input"  id="articleCreateTime" name="articleCreateTime" value="${article.articleCreateTime?c}" />
        </div>

        <div>
            <label class="form-label" for="articleUpdateTime">${updateTimeLabel}</label>
            <input class="form-input"  id="articleUpdateTime" name="articleUpdateTime" value="${article.articleUpdateTime?c}" />
        </div>

        <div>
            <label class="form-label" for="syncWithSymphonyClient">${symClientSyncLabel}</label>
            <input class="form-input"  id="syncWithSymphonyClient" name="syncWithSymphonyClient" value="${article.syncWithSymphonyClient?c}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="clientArticleId">${clientArticleIdLabel}</label>
            <input class="form-input"  id="clientArticleId" name="clientArticleId" value="${article.clientArticleId}" readonly="readonly" />
        </div>

        <div>
            <label class="form-label" for="articleEditorType">${eidotrTypeLabel}</label>
            <input class="form-input"  id="articleEditorType" name="articleEditorType" value="${article.articleEditorType}" readonly="readonly" />
        </div>

        <hr>
        <!-- The following items are modifiable -->
        <div>
            <label class="form-label" for="articleTitle">${titleLabel}</label>
            <input class="form-input"  id="articleTitle" name="articleTitle" value="${article.articleTitle}" />
        </div>

        <div>
            <label class="form-label" for="articleContent">${contentLabel1}</label>
            <textarea class="form-input"  id="articleContent" name="articleContent">
                        ${article.articleContent}
            </textarea>
        </div>

        <div>
            <label class="form-label" for="articleCommentable">${commentableLabel}</label>
            <input class="form-input"  id="articleCommentable" name="articleCommentable" value="${article.articleCommentable?c}" />
        </div>

        <div>
            <label class="form-label" for="articleStatus">${articleStatusLabel}</label>
            <input class="form-input"  id="articleStatus" name="articleStatus" value="${article.articleStatus}" />
        </div>
        <input type="submit" value="${submitLabel}" />
    </form>
</div>
</@admin>