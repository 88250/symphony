<div class="article-list list">
    <ul>
        <#list 1..10 as i>
        <li>
            <div>
                <h2><a href="">title</a></h2>
                <span class="ft-small">
                    <a href="">tagss</a> 
                    2012-02-10
                </span>
                <div class="count ft-small">
                    评论数：<a href="">123</a>
                    访问数：<a href="">123</a>
                </div>
                <div class="fn-right fn-box">
                    <#list 1..10 as i>
                    <img class="avatar-small" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                    </#list>
                </div>
            </div>
            <div class="abstract">abstractabstractabstractabstractabstractabstract</div>
        </li>
        </#list>
    </ul>
</div>