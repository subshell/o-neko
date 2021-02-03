package io.oneko.helmapi.api;

public class HelmTestStrings {

	public static final String helm_search_repo_sophora = "[{\"name\":\"sophora/sophora-server\",\"version\":\"0.1.0\",\"app_version\":\"4\",\"description\":\"A Helm chart for the Sophora Server\"}]";

	public static final String helm_status_oneko = "{\n" +
			"    \"name\": \"oneko\",\n" +
			"    \"info\": {\n" +
			"        \"first_deployed\": \"2020-01-17T17:24:04.66404366+01:00\",\n" +
			"        \"last_deployed\": \"2020-05-28T15:01:42.291082475+02:00\",\n" +
			"        \"deleted\": \"\",\n" +
			"        \"description\": \"Upgrade complete\",\n" +
			"        \"status\": \"deployed\"\n" +
			"    },\n" +
			"    \"manifest\": \"not relevant for this test :-)\",\n" +
			"    \"version\": 16,\n" +
			"    \"namespace\": \"oneko\"\n" +
			"}";

	public static final String helm_list_all_namespaces = "[{\"name\":\"bitwarden\",\"namespace\":\"bitwarden\",\"revision\":\"7\",\"updated\":\"2020-06-02T20:43:34+02:00\",\"status\":\"deployed\",\"chart\":\"bitwarden-1.15.0\",\"app_version\":\"1.15.0\"},{\"name\":\"bitwarden-mysql\",\"namespace\":\"bitwarden\",\"revision\":\"1\",\"updated\":\"2020-05-22T15:09:51+02:00\",\"status\":\"deployed\",\"chart\":\"mysql-6.13.0\",\"app_version\":\"8.0.20\"},{\"name\":\"kafdrop\",\"namespace\":\"sophora-kafka\",\"revision\":\"1\",\"updated\":\"2020-10-02T16:48:56+02:00\",\"status\":\"deployed\",\"chart\":\"kafdrop-0.1.0\",\"app_version\":\"3.x\"},{\"name\":\"mysql-backup\",\"namespace\":\"bitwarden\",\"revision\":\"13\",\"updated\":\"2020-05-29T15:37:06+02:00\",\"status\":\"deployed\",\"chart\":\"mysql-backup-1.0.0\",\"app_version\":\"1.0.0\"},{\"name\":\"oneko\",\"namespace\":\"oneko\",\"revision\":\"16\",\"updated\":\"2020-05-28T15:01:42+02:00\",\"status\":\"deployed\",\"chart\":\"oneko-0.1.0\",\"app_version\":\"1.0\"},{\"name\":\"sophora-kafka-release\",\"namespace\":\"sophora-kafka\",\"revision\":\"1\",\"updated\":\"2020-10-13T15:34:01+02:00\",\"status\":\"deployed\",\"chart\":\"kafka-11.8.6\",\"app_version\":\"2.6.0\"}]";

	public static final String helm_list = "[{\"name\":\"oneko\",\"namespace\":\"oneko\",\"revision\":\"16\",\"updated\":\"2020-05-28T15:01:42+02:00\",\"status\":\"deployed\",\"chart\":\"oneko-0.1.0\",\"app_version\":\"1.0\"}]";

	public static final String helm_repo_list = "[{\"name\":\"bitnami\",\"url\":\"https://charts.bitnami.com/bitnami\"},{\"name\":\"sophora\",\"url\":\"https://docker.subshell.com/chartrepo/sophora\"},{\"name\":\"subshell-tools\",\"url\":\"https://docker.subshell.com/chartrepo/tools\"}]";

	public static final String helm_install = "{\n" +
			"    \"name\": \"ontest\",\n" +
			"    \"info\": {\n" +
			"        \"first_deployed\": \"2021-01-06T18:25:06.512033+01:00\",\n" +
			"        \"last_deployed\": \"2021-01-06T18:25:06.512033+01:00\",\n" +
			"        \"deleted\": \"\",\n" +
			"        \"description\": \"Install complete\",\n" +
			"        \"status\": \"deployed\"\n" +
			"    },\n" +
			"    \"chart\": {\n" +
			"        \"metadata\": {\n" +
			"            \"name\": \"o-neko\",\n" +
			"            \"sources\": [\n" +
			"                \"https://github.com/subshell/o-neko/\"\n" +
			"            ],\n" +
			"            \"version\": \"0.1.0\",\n" +
			"            \"description\": \"A Helm chart for O-Neko\",\n" +
			"            \"maintainers\": [\n" +
			"                {\n" +
			"                    \"name\": \"Team Weasel\",\n" +
			"                    \"email\": \"team-weasel@subshell.com\"\n" +
			"                }\n" +
			"            ],\n" +
			"            \"apiVersion\": \"v2\",\n" +
			"            \"appVersion\": \"1.0.0\",\n" +
			"            \"type\": \"application\"\n" +
			"        },\n" +
			"        \"lock\": null,\n" +
			"        \"templates\": [\n" +
			"            {\n" +
			"                \"name\": \"templates/_helpers.tpl\",\n" +
			"                \"data\": \"data\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"templates/configmap.yaml\",\n" +
			"                \"data\": \"data\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"templates/deployment.yaml\",\n" +
			"                \"data\": \"data\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"templates/ingress.yaml\",\n" +
			"                \"data\": \"data\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"templates/o-neko-clusterrole.yaml\",\n" +
			"                \"data\": \"data\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"templates/o-neko-clusterrolebinding.yaml\",\n" +
			"                \"data\": \"data\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"templates/o-neko-serviceaccount.yaml\",\n" +
			"                \"data\": \"data\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"templates/service.yaml\",\n" +
			"                \"data\": \"data\"\n" +
			"            }\n" +
			"        ],\n" +
			"        \"values\": {\n" +
			"            \"fullnameOverride\": \"\",\n" +
			"            \"hostAliases\": null,\n" +
			"            \"imagePullSecrets\": [],\n" +
			"            \"ingress\": {\n" +
			"                \"annotations\": {},\n" +
			"                \"host\": \"\"\n" +
			"            },\n" +
			"            \"javaOptions\": \"-XX:MinRAMPercentage=50.0 -XX:MaxRAMPercentage=90.0\",\n" +
			"            \"nameOverride\": \"\",\n" +
			"            \"oneko\": {\n" +
			"                \"config\": null,\n" +
			"                \"credentialsCoderKeySecret\": {\n" +
			"                    \"fieldName\": \"key\",\n" +
			"                    \"name\": null\n" +
			"                },\n" +
			"                \"image\": {\n" +
			"                    \"imagePullPolicy\": \"Always\",\n" +
			"                    \"name\": \"subshellgmbh/o-neko\",\n" +
			"                    \"tag\": \"latest-dev\"\n" +
			"                },\n" +
			"                \"mongodb\": {\n" +
			"                    \"secret\": {\n" +
			"                        \"name\": null,\n" +
			"                        \"uriField\": \"uri\"\n" +
			"                    }\n" +
			"                }\n" +
			"            },\n" +
			"            \"resources\": {},\n" +
			"            \"service\": {\n" +
			"                \"annotations\": {},\n" +
			"                \"clusterIP\": null,\n" +
			"                \"loadBalancerIP\": null,\n" +
			"                \"sessionAffinity\": \"None\",\n" +
			"                \"sessionAffinityConfig\": {},\n" +
			"                \"type\": null\n" +
			"            },\n" +
			"            \"serviceAccountName\": null\n" +
			"        },\n" +
			"        \"schema\": null,\n" +
			"        \"files\": [\n" +
			"            {\n" +
			"                \"name\": \".helmignore\",\n" +
			"                \"data\": \"not relevant\"\n" +
			"            },\n" +
			"            {\n" +
			"                \"name\": \"test-values.yaml\",\n" +
			"                \"data\": \"not relevant\"\n" +
			"            }\n" +
			"        ]\n" +
			"    },\n" +
			"    \"config\": {\n" +
			"        \"ingress\": {\n" +
			"            \"host\": \"oneko.subshell.cloud\"\n" +
			"        },\n" +
			"        \"oneko\": {\n" +
			"            \"config\": {\n" +
			"                \"spring\": {\n" +
			"                    \"data\": {\n" +
			"                        \"mongodb\": {\n" +
			"                            \"autoIndexCreation\": true\n" +
			"                        }\n" +
			"                    }\n" +
			"                }\n" +
			"            },\n" +
			"            \"credentialsCoderKeySecret\": {\n" +
			"                \"name\": \"o-neko-credentials-coder-key\"\n" +
			"            },\n" +
			"            \"mongodb\": {\n" +
			"                \"secret\": {\n" +
			"                    \"name\": \"mongodb-credentials\"\n" +
			"                }\n" +
			"            }\n" +
			"        },\n" +
			"        \"resources\": {\n" +
			"            \"limits\": {\n" +
			"                \"cpu\": \"1.5\",\n" +
			"                \"memory\": \"6G\"\n" +
			"            },\n" +
			"            \"requests\": {\n" +
			"                \"cpu\": \"0.5\",\n" +
			"                \"memory\": \"2G\"\n" +
			"            }\n" +
			"        },\n" +
			"        \"serviceAccountName\": \"o-neko-sa\"\n" +
			"    },\n" +
			"    \"manifest\": \"not relevant for this test :-)\",\n" +
			"    \"version\": 1,\n" +
			"    \"namespace\": \"deletethispls\"\n" +
			"}";

	public static final String ONEKO_VALUES = "resources:\n" +
			"  requests:\n" +
			"    memory: \"2G\"\n" +
			"    cpu: \"0.5\"\n" +
			"  limits:\n" +
			"    memory: \"6G\"\n" +
			"    cpu: \"1.5\"\n" +
			"\n" +
			"serviceAccountName: o-neko-sa\n" +
			"\n" +
			"oneko:\n" +
			"  mongodb:\n" +
			"    secret:\n" +
			"      name: mongodb-credentials\n" +
			"  credentialsCoderKeySecret:\n" +
			"    name: o-neko-credentials-coder-key\n" +
			"\n" +
			"  config:\n" +
			"    spring:\n" +
			"      data:\n" +
			"        mongodb:\n" +
			"          autoIndexCreation: true\n" +
			"\n" +
			"ingress:\n" +
			"  host: \"oneko.subshell.cloud\"\n";

}
