# Companies House Certified Copies API

## certified-copies.orders.api.ch.gov.uk
API handling CRUD operations on CH Certified Copies Ordering Service

### Requirements
* [Java 21][1]
* [Maven][2]
* [Git][3]

### Getting Started
1. Run `make` to build
2. Run `./start.sh` to run

### Environment Variables
Name | Description | Mandatory | Location
--- | --- | --- | ---
CERTIFIED_COPIES_API_PORT | Port this application runs on when deployed. | ✓ | start.sh
ITEMS_DATABASE | Mongo Database collection | ✓ | chs-configs repo application env file
MONGODB_URL | URL of Mongo Database | ✓ | chs-configs repo application env file
CHS_API_KEY | API Access Key for CHS | ✓ | chs-configs repo environment global_env
API_URL | URL to CHS API | ✓ | chs-configs repo environment global_env
PAYMENTS_API_URL | Payments API URL | ✓ | chs-configs repo environment global_env

### Endpoints
Path | Method | Description
--- | --- | ---
*`/orderable/certified-copies/healthcheck`* | GET | Returns HTTP OK (`200`) with JSON indicating 'up' status, to indicate a healthy application instance.
*`/orderable/certified-copies`* | POST | Returns HTTP CREATED (`201`) with created object JSON.
*`/orderable/certified-copies/{id}`* | GET | Returns HTTP OK (`200`) and object JSON.


[1]: https://www.oracle.com/java/technologies/downloads/#java21
[2]: https://maven.apache.org/download.cgi
[3]: https://git-scm.com/downloads

## What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |order-service                                      | ECS cluster the service belongs to
**Load balancer**      |{env}-apichgovuk & {env}-apichgovuk-private                                              | The load balancer that sits in front of the service
**Concourse link**     |[pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/certified-copies.orders.api.ch.gov.uk)                                | concourse pipeline link in shared services
**Vault link**         |[Vault link](https://vault.platform.aws.chdev.org/ui/vault/secrets/applications/show/development-eu-west-2/cidev/order-service-stack/certified-copies-orders-api)                                              | vault config link


### Vault
- Vault is a secrets management tool that securely stores and controls access to sensitive data.


## Contributing, Testing, and Useful Links

### Contributing
- Please refer to the [ECS development guide](https://companieshouse.atlassian.net/wiki/spaces/~623250955/pages/4320264207/Idiot+s+guide+to+ECS+Changes) documentation for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner plan executes without issues.
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
- [Terraform runners quickstart docs](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart)