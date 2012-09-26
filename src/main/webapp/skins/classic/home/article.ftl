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
            <div class="wrapper">
                <div id="tip"></div>
                <div class="form">
                    <input type="text" id="articleTitle" />
                    <textarea style="height: 300px" id="articleContent"></textarea>
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
                            <button class="red" onclick="Home.postArticle()">Post</button>
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
