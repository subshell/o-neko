# O-Neko ![O-Neko Logo](oneko.svg)

[![CircleCI](https://circleci.com/gh/subshell/o-neko/tree/master.svg?style=svg)](https://circleci.com/gh/subshell/o-neko/tree/master)

O-Neko is a Kubernetes-native application that allows to cross a bridge between developers and other stakeholders by deploying 
development versions of your software into Kubernetes to allow everybody to try and test them.

|       |       |       |
| ----- | ----- | ----- |
| ![O-Neko login screen](./docs/images/login.png) | ![O-Neko dashboard](./docs/images/dashboard.png) | ![O-Neko project overview](./docs/images/project.png) |

## Documentation and installation guide

[You can find our documentation here.](./docs/DOCUMENTATION.md)

## Features

* Deploy development versions (e.g. every branch) to Kubernetes with one click
* Configure projects with native Kubernetes .yaml files
* Automatically re-deploy running versions when the corresponding Docker image has changed (configurable per project and version)
* Automatically stop running versions after a specific time (configurable per project and version)
* Project meshes that allow you to wrap multiple projects into one pool and deploy specific versions of them together,
effectively providing a way to deploy dynamic test server setups with ease
* Select variables that allow fast changes to frequently used configuration settings (e.g. server URLs)

## Pre-requisites

* Generally, nearly every project able to run in Kubernetes can be deployed with O-Neko
* You need Docker containers of all project versions you want to deploy with O-Neko
* You need to provide the Kubernetes .yaml files in a simple template format to configure a project
* O-Neko works with kubernetes versions 1.9.0 - 1.17.0

## How does it work?

* O-Neko is running inside your Kubernetes cluster
* A project consists of a Docker image that resides in any Docker registry. O-Neko is polling all tags that are available for this image and lets you deploy them.
* The configuration is done with native Kubernetes .yaml and template variables. O-Neko provides some variables (e.g. the docker image tag) but you can also define your own. The configuration and the variables can be overridden by specific versions.
* When a version of a project is deployed, O-Neko creates a namespace in Kubernetes to deploy all resources to. If you stop a deployment, the corresponding namespace will be deleted.
* The status of all running deployments is monitored by O-Neko and can be seen in the web frontend

## Installation
The easiest way to install o-neko is to use our helm chart in the folder helm/oneko.

Please make sure, that you specify the url to the k8s cluster (kubernetesUrl) and an authentication token of a k8s user with the cluster-admin role (kubernetesAuthToken).
In a cloud vendor environment theses values are often provided by environment variables and must not be configured manually.
The ingress class (ingressClass) and its host (oneko.subshell.cloud) must be configured as well.

## Contributing

If you want to report an issue or work on O-Neko please read our [contributing page](./CONTRIBUTING.md).

## Original authors

This project has been started in late 2017 as a "lab day" project by [@philmtd](https://github.com/philmtd), [@reinkem](https://github.com/reinkem), [@schwerlaut](https://github.com/schwerlaut), [@steffenkuche](https://github.com/steffenkuche) and [@tom-schoener](https://github.com/tom-schoener).
