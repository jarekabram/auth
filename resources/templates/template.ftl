<#-- @ftlvariable name="user" type="com.sample.model.User" -->

<#macro mainLayout title="Welcome">
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <title>${title} | Authentication</title>
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/pure/0.6.0/pure-min.css">
    <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css">
    <link rel="stylesheet" type="text/css" href="/styles/main.css">
</head>
<body>
<div class="pure-g">
    <div class="sidebar pure-u-1 pure-u-md-1-4">
        <div class="header">
            <div class="brand-title">Auth</div>
            <nav class="nav">
                <table class="nav-list">
                    <tr>
                        <th class="nav-item"><a class="pure-button" href="/">homepage</a></th>
                        <#if user??>
                            <th class="nav-item"><a class="pure-button" href="/user/${user.userId}">my timeline</a></th>
                            <th class="nav-item"><a class="pure-button" href="/logout">sign out
                                [${user.displayName?has_content?then(user.displayName, user.userId)}]</a></th>
                        <#else>
                            <th class="nav-item"><a class="pure-button" href="/register">sign up</a></th>
                            <th class="nav-item"><a class="pure-button" href="/login">sign in</a></th>
                        </#if>
                    </tr>
                </table>
                <!--
                <ul class="nav-list">
                    <li class="nav-item"><a class="pure-button" href="/">homepage</a></li>
                    <#if user??>
                        <li class="nav-item"><a class="pure-button" href="/user/${user.userId}">my timeline</a></li>
                        <li class="nav-item"><a class="pure-button" href="/logout">sign out
                            [${user.displayName?has_content?then(user.displayName, user.userId)}]</a></li>
                    <#else>
                        <li class="nav-item"><a class="pure-button" href="/register">sign up</a></li>
                        <li class="nav-item"><a class="pure-button" href="/login">sign in</a></li>
                    </#if>
                </ul>
                -->
            </nav>
        </div>
    </div>

    <div class="content pure-u-1 pure-u-md-3-4">
        <h2>${title}</h2>
        <#nested />
    </div>
</div>
</body>
</html>
</#macro>
