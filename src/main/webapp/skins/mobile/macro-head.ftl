<#macro head title>
<meta charset="utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" />
<title>${title}</title>
<#nested>
<meta name="copyright" content="B3log" />
<meta http-equiv="Window-target" content="_top" />
<link type="text/css" rel="stylesheet" href="${staticServePath}/css/base${miniPostfix}.css?${staticResourceVersion}" />
<link type="text/css" rel="stylesheet" href="${staticServePath}/css/responsive${miniPostfix}.css?${staticResourceVersion}" />
<link rel="icon" type="image/png" href="${staticServePath}/images/favicon.png" />
<link rel="apple-touch-icon" href="${staticServePath}/images/faviconH.png">
${siteVisitStatCode}
</#macro>