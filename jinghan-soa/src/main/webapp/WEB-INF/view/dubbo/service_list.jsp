<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta charset="utf-8">
        <title>Dubbo Provider Service Register</title>
        <link rel="stylesheet" href="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/css/bootstrap.min.css">
        <script src="http://cdn.static.runoob.com/libs/jquery/2.1.1/jquery.min.js"></script>
        <script src="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    </head>
    <body>
        <div class="list-group">
            <a href="#" class="list-group-item active">
                <h4 class="list-group-item-heading">
                    ZK Service List
                </h4>
            </a>

            <c:forEach items="${nodeTrees}" var="appNode">
                <a href="javascript:void(0);" class="list-group-item">
                    <table class="table">
                        <caption style="font-size:15pt;"><b class="app">${appNode.nodeName}</b></caption>

                        <%--Application--%>
                        <tr>
                            <th width="30%">Application</th>
                            <th width="25%">Provider</th>
                            <th width="15%">Status</th>
                            <th width="15%">Register</th>
                            <th width="15%">UnRegister</th>
                        </tr>
                        <tr class="active">
                            <td class="app" app-name="${appNode.nodeName}">${appNode.nodeName}</td>
                            <td class="provider">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                                        <span class="default_provider">
                                            ${appNode.defaultProvider}
                                        </span>
                                        <span class="caret"></span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <li>${appNode.defaultProvider}</li>
                                        <c:if test="${not empty appNode.alternativeProviders}">
                                            <li class="divider"></li>
                                            <c:forEach items="${appNode.alternativeProviders}" var="provider">
                                                <li class="app_provider">${provider}</li>
                                            </c:forEach>
                                        </c:if>
                                    </ul>
                                </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${appNode.register}">
                                        <font color="green">已接入</font>
                                    </c:when>
                                    <c:otherwise>
                                        <font color="red">未接入</font>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <button type="button" class="btn btn-success appNode" opt="register" provider="${appNode.defaultProvider}">接入</button>
                            </td>
                            <td>
                                <button type="button" class="btn btn-danger appNode" opt="unregister" provider="${appNode.defaultProvider}">移除</button>
                            </td>
                        </tr>

                        <%--Service--%>
                        <tr>
                            <th>Service</th>
                            <th>Provider</th>
                            <th>Status</th>
                            <th>Register</th>
                            <th>UnRegister</th>
                        </tr>
                        <c:forEach items="${appNode.children}" var="serviceNode" varStatus="status">
                            <c:choose>
                                <c:when test="${status.index==0}">
                                    <tr class="active">
                                </c:when>
                                <c:when test="${status.index % 3 == 0}">
                                    <tr class="success">
                                </c:when>
                                <c:when test="${status.index % 3 == 1}">
                                    <tr class="warning">
                                </c:when>
                                <c:when test="${status.index % 3 == 2}">
                                    <tr class="danger">
                                </c:when>
                            </c:choose>
                                        <td>${serviceNode.nodeName}</td>
                                        <td class="provider">
                                            <div class="btn-group">
                                                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                                                    <span class="default_provider">
                                                        ${serviceNode.defaultProvider}
                                                        <c:if test="${empty serviceNode.defaultProvider}">
                                                            192.168.2.?
                                                        </c:if>
                                                    </span>
                                                    <span class="caret"></span>
                                                </button>
                                                <ul class="dropdown-menu" role="menu">
                                                    <li>${serviceNode.defaultProvider}</li>
                                                    <c:if test="${empty serviceNode.defaultProvider}">
                                                        <li>192.168.2.?</li>
                                                    </c:if>
                                                    <c:if test="${not empty serviceNode.alternativeProviders}">
                                                        <li class="divider"></li>
                                                        <c:forEach items="${serviceNode.alternativeProviders}" var="provider">
                                                            <li class="service_provider">${provider}</li>
                                                        </c:forEach>
                                                    </c:if>
                                                </ul>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${serviceNode.register}">
                                                    <font color="green">已接入</font>
                                                </c:when>
                                                <c:otherwise>
                                                    <font color="red">未接入</font>
                                                </c:otherwise>
                                            </c:choose>
                                        <td>
                                            <button type="button" class="btn btn-success serviceNode" opt="register" provider="${serviceNode.defaultProvider}">接入</button>
                                        </td>
                                        <td>
                                            <button type="button" class="btn btn-danger serviceNode" opt="unregister" provider="${serviceNode.defaultProvider}">移除</button>
                                        </td>
                                    </tr>
                        </c:forEach>
                    </table>
                </a>
            </c:forEach>
        </div>
    </body>

    <script type="application/javascript">
        $(function(){
           $(".btn-group").click(function(){
               $(this).addClass("open");
           });

           $(".app").each(function(){
               $(this).html(formatApp($(this).text()))
           });

           /*选择应用*/
           $(".app_provider").click(function () {
               var defaultProvider = $(this).html();
               $(this).parents('div.btn-group').find('span.default_provider').html(defaultProvider);
               $(this).parents('td.provider').siblings("td").find("button.appNode").each(function () {
                   $(this).attr("provider", defaultProvider);
               });
           });

            /*选择服务*/
           $(".service_provider").click(function () {
               var defaultProvider = $(this).html();
               $(this).parents('div.btn-group').find('span.default_provider').html(defaultProvider);
               $(this).parents('td.provider').siblings("td").find("button.serviceNode").each(function () {
                   $(this).attr("provider", defaultProvider);
               });
           });

           /*接入或移除应用*/
           $(".appNode").click(function(){
               var opt = $(this).attr("opt");
               var appName = $(this).parent().siblings("td").eq(0).attr("app-name");
               var appNickName = $(this).parent().siblings("td").eq(0).text();
               var defaultProviderHost = $(this).attr("provider");
               registerApp(opt, appName, appNickName, null, defaultProviderHost)
           });

           /*接入或移除服务*/
           $(".serviceNode").click(function(){
               var opt = $(this).attr("opt");
               var serviceName = $(this).parent().siblings("td").eq(0).text();
               var defaultProviderHost = $(this).attr("provider");
               if(defaultProviderHost == '' || defaultProviderHost.indexOf("?") > -1){
                   alert("没有有效的服务或服务未启用");
                   return;
               }
               registerApp(opt, null, null, serviceName, defaultProviderHost)
           });
        });

        function registerApp(opt, appName, appNickName, serviceName, defaultProviderHost){
            $.post(
                "registerApp/" + opt,
                {
                    "appName" : appName,
                    "serviceName" : serviceName,
                    "defaultProviderHost" : defaultProviderHost,
                    "remoteZk"  : '${remoteZk}',
                    "proxyZk"  : '${proxyZk}'
                },
                function (data) {
                    var hint = opt=='register' ? '接入' : '移除';
                    if(appNickName != null){
                        hint += appNickName;
                    }
                    if(serviceName != null){
                        hint += serviceName;
                    }
                    if(defaultProviderHost != null){
                        hint += '[' + defaultProviderHost + ']';
                    }
                    hint += data ? "成功" : "失败";
                    alert(hint);

                    if(data){
                        $.post(
                            "serviceList",
                            {
                                "remoteZk" : $("input[name=remoteZk]").val(),
                                "proxyZk"  : $("input[name=proxyZk]").val(),
                                "localZk"  : $("input[name=localZk]").val()
                            },
                            function (data) {
                                $("#services_list").html(data);
                            },
                            "html"
                        );
                    }
                },
                "json"
            );
        }

        function formatApp(input) {
            switch (input){
                case "oder" : return "jinghan-order"; break;
                case "mcnt" : return "jinghan-merchant"; break;
                case "pymt" : return "jinghan-payment"; break;
                case "bs" : return "jinghan-backend"; break;
                default: return "jinghan-" + input;
            }
        }
    </script>
</html>
