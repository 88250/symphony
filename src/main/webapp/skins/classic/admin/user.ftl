<#include "macro-admin.ftl">
<@admin "users">
<div class="content">
    <form action="/admin/user/${user.oId}" method="POST">
        <div class="module">
            <div class="module-header">
                <h2>基本信息</h2>
            </div>
            <div class="module-panel form fn-clear">
                <label for="oId">Id</label>
                <input type="text" id="oId" value="${user.oId}" readonly="readonly" />

                <label for="userName">${userNameLabel}</label>
                <input type="text" id="userName" name="userName" value="${user.userName}" readonly="readonly" />

                <label for="userNo">${userNoLabel}</label>
                <input type="text" id="userNo" name="userNo" value="${user.userNo}" readonly="readonly" />

                <label for="userEmail">${userEmailLabel}</label>
                <input type="text" id="userEmail" name="userEmail" value="${user.userEmail}" readonly="readonly" />

                <label for="userArticleCount">${articleCountLabel}</label>
                <input type="text" id="userArticleCount" name="userArticleCount" value="${user.userArticleCount}" readonly="readonly" />

                <label for="userCommentCount">${commentCountLabel}</label>
                <input type="text" id="userCommentCount" name="userCommentCount" value="${user.userCommentCount}" readonly="readonly" />

                <label for="userTagCount">${tagCountLabel}</label>
                <input type="text" id="userTagCount" name="userTagCount" value="${user.userTagCount}" readonly="readonly" />
            </div>
        </div>
        <div class="module">
            <div class="module-header">
                <h2>可修改信息</h2>
            </div>
            <div class="module-panel form fn-clear">
                <label for="userPassword">${userPasswordLabel}</label>
                <input type="text" id="userPassword" name="userPassword" value="${user.userPassword}" />

                <label for="userURL">URL</label>
                <input type="text" id="userURL" name="userURL" value="${user.userURL}" />

                <label for="userQQ">QQ</label>
                <input type="text" id="userQQ" name="userQQ" value="${user.userQQ}" />

                <label for="userIntro">${userIntroLabel}</label>
                <input type="text" id="userIntro" name="userIntro" value="${user.userIntro}" />

                <label for="userRole">${roleLabel}</label>
                <input type="text" id="userRole" name="userRole" value="${user.userRole}" />

                <label for="userStatus">${userStatus}</label>
                <input type="text" id="userStatus" name="userStatus" value="${user.userStatus}" />

                <label for="userB3Key">B3 Key</label>
                <input type="text" id="userB3Key" name="userB3Key" value="${user.userB3Key}" />

                <label for="userB3ClientAddArticleURL">${clientArticleLabel}</label>
                <input type="text" id="userB3ClientAddArticleURL" name="userB3ClientAddArticleURL" value="${user.userB3ClientAddArticleURL}" />

                <label for="userB3ClientUpdateArticleURL">${clientUpdateArticleLabel}</label>
                <input type="text" id="userB3ClientUpdateArticleURL" name="userB3ClientUpdateArticleURL" value="${user.userB3ClientUpdateArticleURL}" />

                <label for="userB3ClientAddCommentURL">${clientCmtLabel}</label>
                <input type="text" id="userB3ClientAddCommentURL" name="userB3ClientAddCommentURL" value="${user.userB3ClientAddCommentURL}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </div>
        </div>
    </form>
</div>
</@admin>