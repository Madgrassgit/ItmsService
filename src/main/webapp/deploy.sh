for i in ./WEB-INF/lib/*
do
  case "$i" in
     *.jar) export CLASSPATH=$CLASSPATH:./$i ;;
  esac
done

echo $CLASSPATH
java org.apache.axis.client.AdminClient -lhttp://192.168.18.6:8080/ItmsService/services/AdminService deploy.wsdd
echo ===================================
echo 发布成功，请检查服务
