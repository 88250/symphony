<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div id="tip"></div>
                <div class="side">
                    <#include "side.ftl">
                </div>
                <div class="home-content article-post">
                    <input type="text" id="articleTitle" />
                    <textarea id="articleContent"></textarea>
                    <input id="articleTags" type="text" />
                    <div class="fn-clear">
                        <div class="fn-left">
                            <div>
                                <input type="checkbox" id="syncWithSymphonyClient"/>${syncWithSymphonyClientLabel}
                            </div>
                            <div>
                                <a href="">guide</a>
                            </div>
                        </div>
                        <div class="fn-right">
                            <button onclick="Home.postArticle()">Post</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            Label.articleTitleErrorLabel = "${articleTitleErrorLabel}";
            Label.articleContentErrorLabel = "${articleContentErrorLabel}";
            Label.articleTagsErrorLabel = "${articleTagsErrorLabel}";
        </script>
    </body>
</html>
