<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - ${addArticleLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="form">
                    <input type="text" id="articleTitle" />
                    <span style="right:2px;top:10px;"></span>
                    <textarea style="height: 300px" id="articleContent"></textarea>
                    <span style="right:2px;top:338px;"></span>
                    <input id="articleTags" type="text" />
                    <span style="right:2px;top:386px;"></span>
                    <div class="fn-clear">
                        <div class="fn-left">
                            <input type="checkbox" id="syncWithSymphonyClient"/> 
                            ${syncWithSymphonyClientLabel}
                            <a href="/post-guide" target="_blank">guide</a>
                        </div>
                        <div class="fn-right">
                            <button class="red" onclick="AddArticle.add()">Post</button>
                        </div>
                    </div>
                    <div id="addArticleTip"></div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script>
            Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
            Label.articleContentErrorLabel = "${articleContentErrorLabel}";
            Label.articleTagsErrorLabel = "${articleTagsErrorLabel}";
            Label.userName = "${userName}";
        </script>
        <script type="text/javascript" src="/js/add-article.js"></script>
    </body>
</html>
