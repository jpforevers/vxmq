# 使用 Maven 进行构建
FROM maven:3.9.6-eclipse-temurin-17 AS vxmq-build
WORKDIR /vxmq
# 复制源代码并打包
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ src/
RUN mvn clean package -DskipTests

# 通过jlink生成自定义JRE
FROM eclipse-temurin:17-alpine AS jre-build
ENV JAVA_HOME=/opt/java/openjdk
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime


# 创建运行时镜像
FROM alpine:3.21

ARG APPLICATION_USER=vxmq

# 创建用户组和用户（推荐指定UID/GID以便于宿主机权限对齐）
RUN addgroup --system $APPLICATION_USER && adduser --system $APPLICATION_USER --ingroup $APPLICATION_USER

# 创建目录并修改权限
RUN mkdir -p /vxmq && \
    chown -R $APPLICATION_USER:$APPLICATION_USER /vxmq && \
    chmod 755 /vxmq

USER $APPLICATION_USER
WORKDIR /vxmq

RUN apk add --no-cache ca-certificates

# 复制jlink生成的JRE
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build --chown=vxmq:vxmq /javaruntime $JAVA_HOME

# 从构建阶段复制打包好的 JAR 文件
COPY --from=vxmq-build --chown=vxmq:vxmq /vxmq/target/*-fat.jar ./vxmq.jar

ENV IGNITE_HOME=/vxmq/ignite
ENV VXMQ_LOGS_DIR=/vxmq/logs

# 暴露端口
EXPOSE 8060 1883

# 启动命令
CMD ["java", \
    # 模块开放参数
    "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED", \
    "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED", \
    "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", \
    "--add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED", \
    "--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED", \
    "--add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED", \
    "--add-opens=java.base/java.io=ALL-UNNAMED", \
    "--add-opens=java.base/java.nio=ALL-UNNAMED", \
    "--add-opens=java.base/java.util=ALL-UNNAMED", \
    "--add-opens=java.base/java.lang=ALL-UNNAMED", \
    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED", \
    # 应用启动参数
    "-jar", "vxmq.jar"]
