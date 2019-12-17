# Installing O-Neko in your cluster

This document will guide you through the process of installing O-Neko in your cluster.

## Before you start

Before *actually* going through the steps to install O-Neko, this section should act as an information or warning (take it
as you want it) to tell you what O-Neko needs to do in your cluster in order to work.

O-Neko's primary background task is to routinely start containers or throw them away, alongside all other resources your
containers use (config maps, ingresses, services, etc.). To do this, O-Neko **creates namespaces** for every deployed version
of every project. A namespace is created when a version is deployed and it is deleted when the version is about to be
stopped.

**Why does it create these namespaces?** Normally in O-Neko you'd want every single deployment (in the sense
of O-Neko, which means every single "version" (docker tag) of your project) to live isolated from other versions of the same
project. In O-Neko you use plain k8s yaml configuration templates to configure a project. Right now O-Neko does not
need to "understand" the various yaml formats. It only replaces the template variables and feeds the files to your cluster.
This is an elegant and simple way to get O-Neko working without having to worry about changes to these files etc.

If O-Neko wanted to deploy everything into one dedicated namespace it would have to make sure that all resources are
created for each version and that no resources are shared between versions (although this could arguably be a nice feature, too).
To achieve this O-Neko would have to make sure that the name of each resource is non-conflicting (e.g. contains a unique string
identifying the version it belongs to). It would also have to find all references to that name in other templates and replace
it there. To avoid mistakes in this automatic replacement (e.g. something should have been replaced but was not or vice versa)
and to focus on bringing O-Neko to a usable state, we decided to avoid this problem by deploying every version into its own
namespace (as the many namespaces didn't bother us *too* much). We are aware that this approach is not suitable for
everybody and this behaviour might be changed in the future.

## Preparing the deployment

The [k8s-deployment](../../k8s-deployment) directory contains example yaml files to deploy O-Neko in your cluster.
Please do not just use these files as they are. Read these files and understand them and read this guide before you apply
anything to your cluster.

### Getting an access token for O-Neko

O-Neko needs an access token for your cluster to work. The most simple and basic way of achieving this is by applying
the `o-neko-serviceaccount.yaml` and the `o-neko-clusterrolebinding.yaml` and then running 
`kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep o-neko | awk '{print $1}')`. 
You may **not** want to give O-Neko full admin access to your cluster though (which is what these two files try to do),
so please make sure to generate a token for your cluster in a secure way (which may be an entirely different approach).

### Deploying a MongoDB

O-Neko is using MongoDB for persistence. The directory contains a file `o-neko-mongodb.yaml` which can be used to
deploy a single MongoDB container in your cluster. This should definitely not be used in production. You would want
a MongoDB replica set and we kindly want to advise you to look up guides on how to set up a MongoDB replica set in
kubernetes.

### Configuring O-Neko

Configuring O-Neko is relatively simple as there are few configuration options. The configuration is stored in the
`o-neko-configmap.yaml`.

#### MongoDB connection string

First you need to configure your MongoDB URI, which is configured in this block:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://o-neko-mongodb:27017/o-neko?
```

This assumes your MongoDB is available on the specified hostname and port. In case you have a MongoDB replica set
running you need to adjust this connection string to either contain all nodes or the name of the replica set,
e.g. `mongodb://o-neko-mongodb:27017/o-neko?replicaSet=mySet`. For a full list of configuration options of the
MongoDB connection string please refer to the [official documentation](https://docs.mongodb.com/manual/reference/connection-string/).

#### Security

To store the passwords for Docker registries, O-Neko uses symmetric encryption. The encryption key can be defined by the
following configuration variable:

```yaml
o-neko:
  security:
    credentialsCoderKey: VJxDYI6zT9gLLfY9MyDGf2nxQ8mY7DcECxTDqKIV
```

#### Kubernetes access

The access token can be inserted in the following configuration block alongside the url of your k8s API server:

```yaml
kubernetes:
  auth:
    token: <TOKEN>
  server:
    url: <API_URL>
```

### Choosing a frontend URL

Customize the `o-neko-ingress.yaml` to contain a URL of your choice that should point to the frontend of your O-Neko instance.

## Applying the files

After you customised all configuration files you can simply roll them out in your cluster. When the O-Neko pod is ready
you should be able to open the URL you chose in the previous step in your webbrowser and login with the credentials `admin/admin`.
You can now follow the [getting started guide](./GETTING_STARTED.md).

## Notes

* Currently the `o-neko-deployment.yaml` references the `latest` tag. Please change this to a tag you would like to deploy.
* O-Neko currently cannot be scaled (replicas>1) because right now there would be no way to distinguish between a "master"
replica that actively deploys and stops containers and a "slave" replica that should only become active in case the master
is unavailable. This is on our todo list. :)
