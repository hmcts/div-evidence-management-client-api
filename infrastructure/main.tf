locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  # dm_store_url = "http://${var.document_store_url}-${var.env}.service.${local.ase_name}.internal"
  tactical_em_gateway_url = "https://api-gateway.test.dm.reform.hmcts.net"
  tactical_dm_store_url   = "https://api.test.dm.reform.hmcts.net:4604"
}

module "div-em-client-api" {
  source       = "git@github.com:hmcts/moj-module-webapp.git?ref=master"
  product      = "${var.reform_team}-${var.reform_service_name}"
  location     = "${var.location}"
  env          = "${var.env}"
  ilbIp        = "${var.ilbIp}"
  subscription = "${var.subscription}"
  is_frontend  = false

  app_settings = {
    REFORM_SERVICE_NAME                                   = "${var.reform_service_name}"
    REFORM_TEAM                                           = "${var.reform_team}"
    REFORM_ENVIRONMENT                                    = "${var.env}"
    SERVER_PORT                                           = "${var.evidence_management_client_api_port}"
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                  = "${var.auth_provider_service_client_baseurl}"
    AUTH_PROVIDER_SERVICE_CLIENT_MICROSERVICE             = "${var.auth_provider_service_client_microservice}"
    AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.vault_generic_secret.auth_provider_service_client_key.data["value"]}"
    AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"

    # EVIDENCE_MANAGEMENT_UPLOAD_FILE_URL                   = "${local.dm_store_url}/documents"
    # EVIDENCE_MANAGEMENT_HEALTH_URL                        = "${local.dm_store_url}/health"
    # DOCUMENT_MANAGEMENT_STORE_URL                         = "${local.dm_store_url}"

    EVIDENCE_MANAGEMENT_UPLOAD_FILE_URL = "${local.tactical_em_gateway_url}/documents"
    EVIDENCE_MANAGEMENT_HEALTH_URL      = "${local.tactical_em_gateway_url}/health"
    DOCUMENT_MANAGEMENT_STORE_URL       = "${local.tactical_dm_store_url}"
    HTTP_CONNECT_TIMEOUT                = "${var.http_connect_timeout}"
    HTTP_CONNECT_REQUEST_TIMEOUT        = "${var.http_connect_request_timeout}"
    HTTP_CONNECT_SOCKET_TIMEOUT         = "${var.http_connect_socket_timeout}"
  }
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "auth_provider_service_client_key" {
  path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceDocumentUpload"
}
