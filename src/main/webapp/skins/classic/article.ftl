<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="article">
                    <div class="fn-clear">
                        <div class="fn-left">
                            <h2 class="title">
                                我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题
                            </h2>
                            <div>
                                <a href="/tag-articles" >tag1</a>
                                <a href="/" >tag1</a>
                                <a href="/" >tag1</a>
                                <a href="/" >tag1</a>
                            </div>
                        </div>
                        <div class="fn-right author">
                            <img src="/images/user-thumbnail.png" />
                            <span class="fn-ellipsis">
                                <a href="/" title="VanessaLiliYuan">VanessaLiliYuan</a>
                            </span>
                        </div>
                    </div>
                    <div class="content">
                        我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                        我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                        我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                    </div>
                    <div class="fn-clear">
                        <span class="fn-right">
                            2012-01-21
                            浏览<a href="/">1</a> 评论<a href="/">1</a>
                        </span>
                    </div>
                </div>

                <div class="comments">
                    <h3>10 comments</h3>
                    <ul>
                        <#list 1..10 as i>
                        <li>
                            <div class="fn-clear">
                                <div class="author fn-left">
                                    <img src="/images/user-thumbnail.png" />
                                    <span class="fn-ellipsis">
                                        <a href="/" title="VanessaLiliYuan">VanessaLiliYuan</a>
                                    </span>
                                </div>
                                <div class="content">
                                    <div>
                                        by <a href="/">VanessaLiliYuan</a> @ <a href="/">Daniel</a> on 2012-01-21
                                    </div>
                                    我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                    我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                    我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                </div>
                            </div>
                        </li>
                        </#list>  
                    </ul>
                    <div style="text-align: center">
                        <a href="/"><<</a>
                        <a href="/">1</a>
                        <a href="/">2</a>
                        <a href="/">3</a>
                        <a href="/">4</a>
                        <a href="/">...</a>
                        <a href="/">>></a>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script type="text/javascript" src="/js/register.js"></script>
        <script>
            Label.sss = "";
        </script>
    </body>
</html>
