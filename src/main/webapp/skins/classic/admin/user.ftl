<#include "../macro-head.ftl">
<#include "../macro-list.ftl">
<#include "../macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel} - ${userAdminLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/admin${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <form action="/admin/user/${user.oId}" method="POST">
                <!-- The following items are unmodifiable -->
                <div class="form-item">
                    <label class="form-label" for="oId">Id</label>
                    <input class="form-input" id="oId" name="oId" value="${user.oId}" readonly="readonly" />
                </div>

                <div>
                    <label class="form-label" for="userName"></label>
                    <input class="form-input"  id="userName" name="userName" value="${user.userName}" readonly="readonly" />
                </div>

                <div>
                    <label class="form-label" for="userNo">${userNoLabel}</label>
                    <input class="form-input"  id="userNo" name="userNo" value="${user.userNo}" readonly="readonly" />
                </div>

                <div>
                    <label class="form-label" for="userEmail">${userEmailLabel}</label>
                    <input class="form-input"  id="userEmail" name="userEmail" value="${user.userEmail}" readonly="readonly" />
                </div>

                <div>
                    <label class="form-label" for="userArticleCount">${articleCountLabel}</label>
                    <input class="form-input"  id="userArticleCount" name="userArticleCount" value="${user.userArticleCount}" readonly="readonly" />
                </div>

                <div>
                    <label class="form-label" for="userCommentCount">${commentCountLabel}</label>
                    <input class="form-input"  id="userCommentCount" name="userCommentCount" value="${user.userCommentCount}" readonly="readonly" />
                </div>

                <div>
                    <label class="form-label" for="userTagCount">${tagCountLabel}</label>
                    <input class="form-input"  id="userTagCount" name="userTagCount" value="${user.userTagCount}" readonly="readonly" />
                </div>
                
                <hr>
                <!-- The following items are modifiable -->
                <div>
                    <label class="form-label" for="userPassword">${userPasswordLabel}</label>
                    <input class="form-input"  id="userPassword" name="userPassword" value="${user.userPassword}" />
                </div>

                <div>
                    <label class="form-label" for="userURL">URL</label>
                    <input class="form-input"  id="userURL" name="userURL" value="${user.userURL}" />
                </div>

                <div>
                    <label class="form-label" for="userQQ">QQ</label>
                    <input class="form-input"  id="userQQ" name="userQQ" value="${user.userQQ}" />
                </div>

                <div>
                    <label class="form-label" for="userIntro">${userIntroLabel}</label>
                    <input class="form-input"  id="userIntro" name="userIntro" value="${user.userIntro}" />
                </div>

                <div>
                    <label class="form-label" for="userRole">${roleLabel}</label>
                    <input class="form-input"  id="userRole" name="userRole" value="${user.userRole}" />
                </div>

                <div>
                    <label class="form-label" for="userStatus">${userStatus}</label>
                    <input class="form-input"  id="userStatus" name="userStatus" value="${user.userStatus}" />
                </div>

                <div>
                    <label class="form-label" for="userB3Key">B3 Key</label>
                    <input class="form-input"  id="userB3Key" name="userB3Key" value="${user.userB3Key}" />
                </div>

                <div>
                    <label class="form-label" for="userB3ClientAddArticleURL">${clientArticleLabel}</label>
                    <input class="form-input"  id="userB3ClientAddArticleURL" name="userB3ClientAddArticleURL" value="${user.userB3ClientAddArticleURL}" />
                </div>

                <div>
                    <label class="form-label" for="userB3ClientUpdateArticleURL">${clientUpdateArticleLabel}</label>
                    <input class="form-input"  id="userB3ClientUpdateArticleURL" name="userB3ClientUpdateArticleURL" value="${user.userB3ClientUpdateArticleURL}" />
                </div>

                <div>
                    <label class="form-label" for="userB3ClientAddCommentURL">${clientCmtLabel}</label>
                    <input class="form-input"  id="userB3ClientAddCommentURL" name="userB3ClientAddCommentURL" value="${user.userB3ClientAddCommentURL}" />
                </div>

                <input type="submit" value="${submitLabel}" />
            </form>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
