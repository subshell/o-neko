# About O-Neko

O-Neko is a Kubernetes-native application that allows to cross a bridge between developers and other stakeholders by
deploying development versions of your software into Kubernetes to allow everybody to try and test them.

## Features

The most important features of O-Neko are:

* Deploy development versions (e.g. every branch) to Kubernetes with one click
* Configure projects with native Kubernetes .yaml files
* Automatically re-deploy running versions when the corresponding Docker image has changed (configurable per project and version)
* Automatically stop running versions after a specific time (configurable per project and version)
* Project meshes that allow you to wrap multiple projects into one pool and deploy specific versions of them together,
effectively providing a way to deploy dynamic test server setups with ease
* Select variables that allow fast changes to frequently used configuration settings (e.g. server URLs)

## Pre-requisites

Generally, nearly every project able to run in Kubernetes can be deployed with O-Neko. To run a project in O-Neko:

* You need Docker containers of all project versions you want to deploy with O-Neko
* You need to provide the Kubernetes .yaml files in a simple template format to configure a project
* O-Neko works with kubernetes versions 1.9.0 - 1.15.3

## How does it work?

* O-Neko is running inside your Kubernetes cluster
* A project consists of a Docker image that resides in any Docker registry. O-Neko is polling all tags that are available for this image and lets you deploy them.
* The configuration is done with native Kubernetes .yaml and template variables. O-Neko provides some variables (e.g. the docker image tag), but you can also define your own. The configuration and the variables can be overridden by specific versions.
* When a version of a project is deployed, O-Neko creates a namespace in Kubernetes to deploy all resources to. If you stop a deployment, the corresponding namespace will be deleted.
* The status of all running deployments is monitored by O-Neko and can be seen on the web frontend

## O-Neko vocabulary

In O-Neko as well as in this documentation we often use terms that may be misunderstood for something else. We want
to clarify what specific words mean in the context of O-Neko.

### Project
A project is the entry point for deploying an application with O-Neko. Projects have a direct
association with a Docker image. Theoretically you can also have multiple projects referencing the same Docker image.
A project can have multiple versions. Projects can be configured using configuration templates.

### Project Version

A project version is associated with a tag of the project's docker image.
A project version inherits the project's configuration templates but can also override them or add entirely new configuration
templates. A project version can be deployed and automatically updated if the docker tag gets pushed again with different
contents (based on the checksum).

### Configuration template

A configuration template is a native kubernetes .yaml file (e.g. a service definition).
Variable placeholders like `${MY_VARIABLE_NAME}` can be used to insert O-Neko specific variables into these templates.
The variables can be overridden in project versions to e.g. achieve slightly different configurations of versions without
having to modify the templates per version.

### Select variables

Select variables are template variables with pre-defined values. If (e.g.) your application
needs to connect to an external service E, you could have a variable consisting of the hostnames of the production, test
and development versions of E and you could quickly choose which instance of E to connect to from the O-Neko dashboard.

### Project mesh

A project mesh is a conglomerate of specific project versions. They can be used to create "static"
test setups that get automatically updated when the software changes. Example use case: Let's say you are developing a software that
consists of multiple programs (e.g. server + tools, microservices, etc.). You could e.g. set up a project mesh for every
major version of your software that is still actively maintained to get a "full" test setup.

## Users and roles

O-Neko has user management with a very basic permission scheme consisting of three roles:

| Role | Description |
| --- | --- |
| VIEWER | Can view projects (and versions) and deploy them or stop deployments. Can not modify them or change the configurations except of select variables that are displayed on the O-Neko dashboard. |
| DOER | Can also create and modify projects. |
| ADMIN | Can also manage users, docker registries and namespaces. |
