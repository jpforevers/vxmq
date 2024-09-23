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
 docker run -d --name vxmq -p 1883:1883 -p 5000:5000 -p 8060:8060 jpforevers/vxmq:1.7.0
```
