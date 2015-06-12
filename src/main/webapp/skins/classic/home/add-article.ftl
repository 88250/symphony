<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${addArticleLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror/codemirror.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="form">
                    <div>
                        <input type="text" id="articleTitle" value="<#if article??>${article.articleTitle}</#if>" placeholder="${titleLabel}" />
                        <span style="right:2px;top:41px;"></span>
                    </div>
                    <label class="article-content-label">
                        ${contentLabel}
                        <a href="javascript:AddArticle.grammar()">${baseGrammarLabel}</a>
                        <a target="_blank" href="http://daringfireball.net/projects/markdown/syntax">${allGrammarLabel}</a>
                    </label>

                    <div class="fn-clear">
                        <div class="fn-left article-content">
                            <textarea id="articleContent"><#if article??>${article.articleContent}</#if></textarea>
                            <span id="articleContentTip" style="top: 429px; right: 2px;"></span>
                        </div>
                        <div class="fn-left grammar fn-none">
                            ${markdwonGrammarLabel}
                        </div>
                    </div>
                    <div>
                        <input id="articleTags" type="text" value="<#if article??>${article.articleTags}</#if>" placeholder="${tagLabel}（${tagSeparatorTipLabel}）"/>
                        <span style="right:2px;top:507px;"></span><br/><br/>
                    </div>
                    <div class="fn-clear">
                        <div class="fn-left">
                            <input<#if article??> disabled="disabled"<#if article.syncWithSymphonyClient> checked="checked"</#if></#if> type="checkbox" id="syncWithSymphonyClient"/> 
                                ${syncWithSymphonyClientLabel}
                        </div>
                        <div class="fn-left" style="margin-left: 5px;">
                            <input<#if article??><#if article.articleCommentable> checked="checked"</#if><#else> checked="checked"</#if> type="checkbox" id="articleCommentable"/> 
                                ${commentableLabel}
                        </div>
                        <div class="fn-left" style="margin-left: 5px;">
                            <input<#if article?? && 1 == article.articleType> checked="checked"</#if> type="checkbox" id="articleType"/> 
                                ${discussionLabel}
                        </div>
                        <div class="fn-right">
                            <button class="green<#if !article??> fn-none</#if>" onclick="AddArticle.preview()">${previewLabel}</button> &nbsp; &nbsp; 
                            <button class="red" onclick="AddArticle.add(<#if article??>'${article.oId}'</#if>)"><#if article??>${editLabel}<#else>${postLabel}</#if></button>
                        </div>
                    </div>
                    <div id="addArticleTip">
                    </div>
                    ${postGuideLabel}
                </div>
            </div>
        </div>
        <div id="preview" class="content-reset"></div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/codemirror/codemirror.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/add-article${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                                Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
                                                Label.articleContentErrorLabel = "${articleContentErrorLabel}";
                                                Label.articleTagsErrorLabel = "${articleTagsErrorLabel}";
                                                Label.userName = "${userName}";
        </script>
    </body>
</html>
