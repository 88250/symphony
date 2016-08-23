<#include "macro-admin.ftl">
<@admin "addArticle">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${addArticleLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/add-article" method="POST">
                <label>${userNameLabel}</label>
                <input name="userName" type="text" />

                <label>${timeLabel}</label>
                <input name="time" type="datetime-local" />

                <label>${titleLabel}</label>
                <input name="articleTitle" type="text" />

                <label>${tagLabel}</label>
                <input name="articleTags" type="text" />

                <label>${contentLabel}</label>
                <textarea name="articleContent" rows="20"></textarea>

                <label>${rewardContentLabel}</label>
                <textarea name="articleRewardContent" rows="20"></textarea>

                <label>${rewardPointLabel}</label>
                <input name="articleRewardPoint" type="number" value="0" />

                <br/><br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>   
</div>
</@admin>