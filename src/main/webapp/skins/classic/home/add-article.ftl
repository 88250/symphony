<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${addArticleLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/codemirror/codemirror.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="form">
                    ${titleLabel}
                    <div>
                        <input type="text" id="articleTitle" value="<#if article??>${article.articleTitle}</#if>" />
                        <span style="right:2px;top:30px;"></span>
                    </div>
                    ${contentLabel}
                    <a href="javascript:AddArticle.grammar()">${baseGrammarLabel}</a>
                    <a target="_blank" href="http://daringfireball.net/projects/markdown/syntax">${allGrammarLabel}</a>
                    <div class="fn-clear">
                        <div class="fn-left">
                            <textarea id="articleContent"><#if article??>${article.articleContent}</#if></textarea>
                            <span id="articleContentTip" style="right:2px;top:410px;"></span>
                        </div>
                        <div class="fn-left grammar fn-none">
                            ${markdwonGrammarLabel}
                        </div>
                    </div>
                    ${tagLabel}
                    <div>
                        <input id="articleTags" type="text" value="<#if article??>${article.articleTags}</#if>" />
                        <span style="right:2px;top:478px;"></span>
                    </div>
                    <div class="fn-clear">
                        <div class="fn-left">
                            <input<#if article??> disabled="disabled"<#if article.syncWithSymphonyClient> checked="checked"</#if></#if> type="checkbox" id="syncWithSymphonyClient"/> 
                            ${syncWithSymphonyClientLabel}
                        </div>
                        <div class="fn-right">
                            <button class="green<#if !article??> fn-none</#if>" onclick="AddArticle.preview()">${previewLabel}</button>
                            <button class="red" onclick="AddArticle.add(<#if article??>'${article.oId}'</#if>)">${postLabel}</button>
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
        <script>
            Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
            Label.articleContentErrorLabel = "${articleContentErrorLabel}";
            Label.articleTagsErrorLabel = "${articleTagsErrorLabel}";
            Label.userName = "${userName}";
        </script>
        <script src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/codemirror/codemirror.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/add-article.js?${staticResourceVersion}"></script>
    </body>
</html>
