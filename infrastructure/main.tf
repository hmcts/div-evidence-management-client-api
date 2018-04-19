locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  dm_store_url = "http://dm-store-${var.env}.service.${local.ase_name}.internal"
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
    AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.vault_generic_secret.idam-auth-secret.data["value"]}"
    AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"
    DIVORCE_DOCUMENT_UPLOAD_KEY                           = "${data.vault_generic_secret.divorce_document_upload_client_key.data["value"]}"

    DOCUMENT_MANAGEMENT_STORE_URL       = "${local.dm_store_url}"
    EVIDENCE_MANAGEMENT_UPLOAD_FILE_URL = "${local.dm_store_url}/documents"
    EVIDENCE_MANAGEMENT_HEALTH_URL      = "${local.dm_store_url}/health"
    HTTP_CONNECT_TIMEOUT                = "${var.http_connect_timeout}"
    HTTP_CONNECT_REQUEST_TIMEOUT        = "${var.http_connect_request_timeout}"
    HTTP_CONNECT_SOCKET_TIMEOUT         = "${var.http_connect_socket_timeout}"
  }
}

# region save DB details to Azure Key Vault
module "key-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  # dcd_cc-dev group object ID
  product_group_object_id = "38f9dea6-e861-4a50-9e73-21e64f563537"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location_app}"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "idam-auth-secret" {
  path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceDocumentGenerator"
}

data "vault_generic_secret" "divorce_document_upload_client_key" {
  path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceDocumentGenerator"
}
