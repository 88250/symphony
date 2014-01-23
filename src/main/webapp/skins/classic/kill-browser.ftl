<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>B3log ${symphonyLabel} - ${visionLabel}</title>
        <meta name="keywords" content="${visionLabel},B3log ${symphonyLabel}"/>
        <meta name="description" content="B3log ${symphonyLabel}${b3logDescriptionLabel}"/>
        <meta name="owner" content="B3log Team" />
        <meta name="author" content="B3log Team" />
        <meta name="copyright" content="B3log" />
        <link rel="icon" type="image/png" href="${staticServePath}/favicon.png" />
    </head>
    <body>
        <div class="wrapper">
            <div class="wrap">
                <div class="content" style="top:-6px">
                    <div class="logo">
                        <a href="http://b3log.org" target="_blank">
                            <img border="0" width="153" height="56" alt="B3log" title="B3log" src="${staticServePath}/images/logo.jpg"/>
                        </a>
                    </div>
                    <div class="main kill" style="height: 385px;">
                        ${killBrowserLabel}
                        <br/>
                         &nbsp; &nbsp;&nbsp; <button onclick="closeIframe();">{closeLabel}</button> &nbsp; &nbsp; 
                        <button onclick="closeIframeForever();">{closeForeverLabel}</button>
                        <img src='${staticServePath}/images/kill-browser.png' title='Kill IE6' alt='Kill IE6'/>
                        <a href="http://b3log.org" target="_blank">
                            <img border="0" class="icon" alt="B3log" title="B3log" src="${staticServePath}/favicon.png"/>
                        </a>
                    </div>
                    <span class="clear"></span>
                </div>
            </div>
        </div>
        <script>
            var closeIframe = function () {
                window.parent.$("iframe").prev().remove();
                window.parent.$("iframe").remove();
            };
            
            var closeIframeForever = function () {
                window.parent.Cookie.createCookie("showKill", true, 365);
                closeIframe();
            };
        </script>
    </body>
</html>
