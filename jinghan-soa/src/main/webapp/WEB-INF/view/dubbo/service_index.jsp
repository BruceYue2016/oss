<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
        <%--ZK Bridge begin--%>
        <div class="list-group">
            <a href="#" class="list-group-item active">
                <h4 class="list-group-item-heading">
                    ZK Bridge
                </h4>
            </a>
            <div style="height:80px;width:100%;display:flex;justify-content: space-between;">
                <a href="#" class="list-group-item" style="border-radius:70%;">
                    <h4 class="list-group-item-heading">
                        Remote ZK
                    </h4>
                    <input type="text" name="remoteZk" value="192.168.2.6:2181">
                </a>
                <a href="#" class="list-group-item" style="border-radius:70%;">
                    <h4 class="list-group-item-heading">
                        Proxy ZK <em title="port = 本机 IP MAC 地址 + 2181, 比如我的ip是192.168.2.135, port=135+2181">接入</em>
                    </h4>
                    <input type="text" name="proxyZk" value="${proxyZk}">
                </a>
                <a href="#" class="list-group-item" style="border-radius:70%;">
                    <h4 class="list-group-item-heading">
                        Local ZK <em title="请先在本地主机上创建ZK服务器(127.0.0.1:2181), 选择本地后会优先于代理ZK服务">接入</em>
                    </h4>
                    <input type="text" name="localZk" value="">
                </a>
            </div>
        </div>
        <%--ZK Bridge end--%>

        <div id="services_list">
            <div class="list-group">
                <a href="#" class="list-group-item active">
                    <h4 class="list-group-item-heading">
                        ZK Service List
                    </h4>
                </a>
            </div>
        </div>

        <script type="application/javascript">
            $(function(){
                if($("input[name=proxyZk]").val() != ''){
                    loadServicePage();
                }

                $('em').click(function () {
                    loadServicePage();
                });
            });

            function loadServicePage(){
                var remoteZk = $("input[name=remoteZk]").val();
                if(remoteZk == "192.168.2.6:2181" || remoteZk == "192.168.2.9:2181" || remoteZk == "192.168.2.11:2181" || remoteZk == "192.168.2.143:2181"){
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
                }else{
                    alert("remoteZk only can be chosen from 192.168.2.6:2181 or 192.168.2.9:2181 or 192.168.2.11:2181 or 192.168.2.143:2181");
                }
            }
        </script>

    </body>
</html>