# RestBlue
REST API for BLE GATT on Android
===================================

This Android project provides a REST API over HTTP channel which aims to cover all BLE GATT interactions. The HTTP server is built is using nanoHTTPD, and hence is meant to be used for relatively light-weight loads (say, few clients).

Introduction
------------

Things are rather straight forward. BLE GATT interactions, which are service-oriented (not meaning GATT Services), are modeled as resources. Service calls such as starting LE scan, connection, characterstic/descriptor read/write are represented as resources that need to be created (POST) which then have some state. This philosophy follows to a large extent the intent behing Web of Things.

The Path
--------

- [x] LE Scanning
- [ ] Connection
- [ ] Read and Write
- [ ] Notifications

Getting Started
---------------

This project uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.