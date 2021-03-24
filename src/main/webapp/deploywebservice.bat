java -cp ./WEB-INF/lib/axis.jar;./WEB-INF/lib/wsdl4j-1.5.1.jar;./WEB-INF/lib/jaxrpc.jar;./WEB-INF/lib/commons-logging-1.0.4.jar;./WEB-INF/lib/commons-discovery-0.2.jar;./WEB-INF/lib/wsdl4j-1.5.1.jar org.apache.axis.client.AdminClient -l http://202.102.39.141:7070/ItmsService/services/AdminService deploy.wsdd
echo ===================================
echo 发布成功，请检查服务
echo. & pause