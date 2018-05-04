locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  dm_store_url = "http://dm-store-${var.env}.service.${local.ase_name}.internal"
  idam_s2s_url = "http://${var.idam_s2s_url_prefix}-${var.env}.service.${local.ase_name}.internal"
}

module "div-emca" {
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
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                  = "${local.idam_s2s_url}"
    AUTH_PROVIDER_SERVICE_CLIENT_MICROSERVICE             = "${var.auth_provider_service_client_microservice}"
    AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.vault_generic_secret.div-doc-s2s-auth-secret.data["value"]}"
    AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"
    DIVORCE_DOCUMENT_UPLOAD_KEY                           = "${data.vault_generic_secret.divorce_document_upload_client_key.data["value"]}"

    DOCUMENT_MANAGEMENT_STORE_URL       = "${local.dm_store_url}"
    EVIDENCE_MANAGEMENT_UPLOAD_FILE_URL = "${local.dm_store_url}/documents"
    EVIDENCE_MANAGEMENT_HEALTH_URL      = "${local.dm_store_url}/health"
    HTTP_CONNECT_TIMEOUT                = "${var.http_connect_timeout}"
    HTTP_CONNECT_REQUEST_TIMEOUT        = "${var.http_connect_request_timeout}"
    HTTP_CONNECT_SOCKET_TIMEOUT         = "${var.http_connect_socket_timeout}"
    IDAM_API_BASEURL = "${var.idam_api_baseurl}"
    IDAM_API_HEALTH_URI = "${var.idam_api_baseurl}/health"
  }
}

# region save DB details to Azure Key Vault
module "key-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${var.product}-${var.component}-${var.env}"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.div-emca.resource_group_name}"

  # dcd_cc-dev group object ID
  product_group_object_id = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "div-doc-s2s-auth-secret" {
  path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceDocumentGenerator"
}

data "vault_generic_secret" "divorce_document_upload_client_key" {
  path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceDocumentGenerator"
}

resource "azurerm_key_vault_secret" "div-doc-s2s-auth-secret" {
  name      = "div-doc-s2s-auth-secret"
  value     = "${data.vault_generic_secret.div-doc-s2s-auth-secret.data["value"]}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}
