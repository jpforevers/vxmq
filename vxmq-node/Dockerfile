# 使用 Maven 进行构建
FROM maven:3.9.6-eclipse-temurin-17 AS vxmq-build
WORKDIR /vxmq
# 复制源代码并打包
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src/ src/
RUN mvn clean package -DskipTests

# 创建运行时镜像
FROM eclipse-temurin:17-alpine
RUN apk add --no-cache ca-certificates
ARG APPLICATION_USER=vxmq
# 创建用户组和用户（推荐指定UID/GID以便于宿主机权限对齐）
RUN addgroup --system $APPLICATION_USER && adduser --system $APPLICATION_USER --ingroup $APPLICATION_USER
# 创建目录并修改权限
RUN mkdir -p /vxmq && \
    chown -R $APPLICATION_USER:$APPLICATION_USER /vxmq && \
    chmod 750 /vxmq
USER $APPLICATION_USER
WORKDIR /vxmq
# 从构建阶段复制打包好的 JAR 文件
COPY --from=vxmq-build --chown=$APPLICATION_USER:$APPLICATION_USER /vxmq/target/vxmq-node-*-fat.jar ./vxmq.jar
# 暴露端口
EXPOSE 8060 1883
# 环境变量
ENV IGNITE_HOME=/vxmq/ignite
ENV VXMQ_LOGS_DIR=/vxmq/logs
ENV JAVA_OPTS="--add-opens java.base/jdk.internal.access=ALL-UNNAMED \
               --add-opens java.base/jdk.internal.misc=ALL-UNNAMED \
               --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
               --add-opens java.base/sun.util.calendar=ALL-UNNAMED \
               --add-opens java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
               --add-opens jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
               --add-opens java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
               --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
               --add-opens java.base/java.io=ALL-UNNAMED \
               --add-opens java.base/java.nio=ALL-UNNAMED \
               --add-opens java.base/java.net=ALL-UNNAMED \
               --add-opens java.base/java.util=ALL-UNNAMED \
               --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
               --add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED \
               --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED \
               --add-opens java.base/java.lang=ALL-UNNAMED \
               --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
               --add-opens java.base/java.math=ALL-UNNAMED \
               --add-opens java.sql/java.sql=ALL-UNNAMED \
               --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
               --add-opens java.base/java.time=ALL-UNNAMED \
               --add-opens java.base/java.text=ALL-UNNAMED \
               --add-opens java.management/sun.management=ALL-UNNAMED \
               --add-opens java.desktop/java.awt.font=ALL-UNNAMED"
# 启动命令
CMD ["sh", "-c", "java $JAVA_OPTS -jar vxmq.jar"]
