<#include "macro-head.ftl">
<#include "macro-footer.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div>
                        <div class="ft-small fn-clear">
                            <div class="fn-left">
                                2012-01-21
                            </div>
                            <div class="fn-right">
                                浏览<a href="/">1</a> 评论<a href="/">1</a>   
                            </div>
                            <div class="fn-right">
                                <a href="/tag-articles" >tag1</a>
                                <a href="/" >tag1</a>
                                <a href="/" >tag1</a>
                                <a href="/" >tag1</a>
                            </div>
                        </div>
                        <h2 class="article-title">
                            <a href="" rel="bookmark">
                                我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题我是标题
                            </a>
                        </h2>
                        <div>
                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                        </div>
                    </div>
                    <div class="comment-list list">
                        <h2>10 comments</h2>
                        <ul>
                            <#list 1..5 as i>
                            <li>
                                <div class="fn-clear">
                                    <div class="fn-left avatar">
                                        <img src="/images/user-thumbnail.png" />
                                    </div>
                                    <div class="fn-left comment-main">
                                        <span class="fn-clear">
                                            <span class="fn-left">
                                                <a href="/" title="VanessaLiliYuan">VanessaLiliYuan</a>
                                                @ <a href="/">Daniel</a>
                                            </span>
                                            <span class="fn-right ft-small">
                                                2012-01-21
                                            </span>    
                                        </span>
                                        <div>
                                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                        </div>
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
                    <div class="form">
                        <textarea></textarea>
                        <button>submit</button>
                    </div>

                </div>
                <div class="side">
                    <div class="index-module">
                        <div class="fn-clear">
                            <div class="fn-left avatar">
                                <img src="/images/user-thumbnail.png" />
                            </div>
                            <div class="fn-left">
                                <a href="/" title="VanessaLiliYuan">VanessaLiliYuan</a><br/>
                                <a href="/" title="VanessaLiliYuan">http://vanessa.b3log.org</a>
                            </div>
                        </div>
                        <div>
                            introintrointrointrointrointrointrointrointrointrointro
                        </div>
                    </div>
                    <div class="index-module">
                        <h2>
                            relative article
                        </h2>
                        <ul>
                            <#list 1..10 as i>
                            <li>
                                <a href="">Recent Post</a> 
                                <div class="ft-small">
                                    <span>
                                        <a href="">vanesaa</a>
                                        2011-1-1
                                    </span>
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <div class="index-module">
                        <h2>
                            随机文章
                        </h2>
                        <ul>
                            <#list 1..10 as i>
                            <li>
                                <a href="">Recent Post</a> 
                                <div class="ft-small">
                                    <span>
                                        <a href="">vanesaa</a>
                                        2011-1-1
                                    </span>
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <@footer/>
    </body>
</html>
