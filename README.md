![Project Logo](./color_logo.png)

# VXMQ

VXMQ is a high performance MQTT Broker - Tailored for Modern IoT Needs.

VXMQ built in Java, fully implementing all features of MQTT 5, specifically designed for modern IoT applications. Whether handling large-scale device connections or real-time data transmission, delivers exceptional performance and reliability.

# Key Features:

* **Full MQTT 5 Support**: Enjoy the latest protocol features, including topic aliases, request/response patterns, and more flexible message delivery mechanisms, providing an enhanced experience for developers and end-users.

* **Robust Clustering Support**: With clustering capabilities, VXMQ can easily scale to handle a vast number of concurrent connections, ensuring stable performance even under high load.

* **Built on Vert.x Framework**: Utilizing the asynchronous, non-blocking architecture of Vert.x, VXMQ achieves higher efficiency and lower latency when processing requests, perfectly addressing the challenges of real-time data streams.

* **Outstanding Performance**: Rigorously tested, VXMQ competes with many mature commercial products in terms of throughput and response time, meeting the demands of various application scenarios.

* **Easy Deployment and Management**: User-friendly configuration options and comprehensive documentation help users get started quickly and manage the Broker effectively.

# Get Started

```
 docker run -d --name vxmq -p 1883:1883 -p 8060:8060 jpforevers/vxmq:latest
```

# Cluster
## docker compose
In the `src/main/docker` folder, there is an example file `docker-compose.yaml` for starting a local cluster through Docker Compose, just `cd` into and typing `docker compose up -d`

## docker in different host

Host1:

```
docker run -d --name vxmq --network host -e VXMQ_HTTP_SERVER_PORT=8060 -e VXMQ_MQTT_SERVER_PORT=1883 -e VXMQ_IGNITE_DISCOVERY_TCP_PORT=47500 -e VXMQ_IGNITE_DISCOVERY_TCP_ADDRESS=192.168.16.197 -e VXMQ_IGNITE_DISCOVERY_TCP_IP_FINDER_MULTICAST_ADDRESSES='192.168.16.197:47500,192.168.16.249:47500' -e VXMQ_VERTX_EVENTBUS_HOST=192.168.16.197 -e VXMQ_VERTX_EVENTBUS_PORT=52014 jpforevers/vxmq:latest
```

Host2:

```
docker run -d --name vxmq --network host -e VXMQ_HTTP_SERVER_PORT=8060 -e VXMQ_MQTT_SERVER_PORT=1883 -e VXMQ_IGNITE_DISCOVERY_TCP_PORT=47500 -e VXMQ_IGNITE_DISCOVERY_TCP_ADDRESS=192.168.16.249 -e VXMQ_IGNITE_DISCOVERY_TCP_IP_FINDER_MULTICAST_ADDRESSES='192.168.16.197:47500,192.168.16.249:47500' -e VXMQ_VERTX_EVENTBUS_HOST=192.168.16.249 -e VXMQ_VERTX_EVENTBUS_PORT=52014 jpforevers/vxmq:latest
```

## kubernetes

VXMQ should be deployed as a Kubernetes StatefulSet.

VXMQ has built-in support for Kubernetes, and you only need to configure the following three environment variables to build a cluster:

* VXMQ_IGNITE_DISCOVERY_TCP_IP_FINDER_TYPE, value is `kubernetes`.

* VXMQ_IGNITE_DISCOVERY_TCP_IP_FINDER_KUBERNETES_NAMESPACE, value is your Kubernetes namespace.

* VXMQ_IGNITE_DISCOVERY_TCP_IP_FINDER_KUBERNETES_SERVICENAME, value is your Kubernetes Headless service name.
