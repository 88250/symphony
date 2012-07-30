<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="robots" content="none" />
        <title>${blogTitle} - 403 Forbidden!</title>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/default-init${miniPostfix}.css?${staticResourceVersion}" charset="utf-8" />
        <link rel="icon" type="image/png" href="${staticServePath}/favicon.png" />
    </head>
    <body>
        <div class="wrapper">
            <div class="wrap">
                <div class="content">
                    <div class="logo">
                        <a href="http://b3log.org" target="_blank">
                            <img border="0" width="153" height="56" alt="B3log" title="B3log" src="${staticServePath}/images/logo.jpg"/>
                        </a>
                    </div>
                    <div class="main">
                        <h2>403 Forbidden!</h2>
                        <img class="img-403" src="${staticServePath}/images/403.png" alt="403: forbidden" title="403: forbidden" />
                        <div class="a-403">
                            <a href="${servePath}">Index</a> |
                            <a href="${loginURL}">Login</a>
                        </div>
                        <a href="http://b3log.org" target="_blank">
                            <img border="0" class="icon" alt="B3log" title="B3log" src="${staticServePath}/favicon.png"/>
                        </a>
                    </div>
                    <span class="clear"></span>
                </div>
            </div>
        </div>
        <div class="footerWrapper">
            <div class="footer">
                &copy; ${year}
                Powered by
                <a href="http://b3log.org" target="_blank">
                    <span style="color: orange;">B</span><span style="color: blue;"><sup>3</sup></span><span style="color: green;">L</span><span style="color: red;">O</span><span style="color: blue;">G</span>
                    <span class="solo">Solo</span>
                </a>
            </div>
        </div>
    </body>
</html>
