locals {
  ase_name       = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env      = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  dm_store_url   = "http://dm-store-${local.local_env}.service.core-compute-${local.local_env}.internal"
  idam_s2s_url   = "http://${var.idam_s2s_url_prefix}-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName = "${var.reform_team}-aat"
  nonPreviewVaultName = "${var.reform_team}-${var.env}"
  vaultName = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"
}

module "div-emca" {
  source                          = "git@github.com:hmcts/moj-module-webapp.git?ref=master"
  product                         = "${var.product}-${var.reform_service_name}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  subscription                    = "${var.subscription}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  capacity                        = "${var.capacity}"
  is_frontend                     = false
  common_tags                     = "${var.common_tags}"

  app_settings = {
    REFORM_SERVICE_NAME                                   = "${var.reform_service_name}"
    REFORM_TEAM                                           = "${var.reform_team}"
    REFORM_ENVIRONMENT                                    = "${var.env}"
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                  = "${local.idam_s2s_url}"
    AUTH_PROVIDER_SERVICE_CLIENT_MICROSERVICE             = "${var.auth_provider_service_client_microservice}"
    AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.azurerm_key_vault_secret.div_doc_s2s_auth_secret.value}"
    AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"

    DOCUMENT_MANAGEMENT_STORE_URL       = "${local.dm_store_url}"
    EVIDENCE_MANAGEMENT_UPLOAD_FILE_URL = "${local.dm_store_url}/documents"
    EVIDENCE_MANAGEMENT_HEALTH_URL      = "${local.dm_store_url}/health"
    HTTP_CONNECT_TIMEOUT                = "${var.http_connect_timeout}"
    HTTP_CONNECT_REQUEST_TIMEOUT        = "${var.http_connect_request_timeout}"
    HTTP_CONNECT_SOCKET_TIMEOUT         = "${var.http_connect_socket_timeout}"
    IDAM_API_URL = "${var.idam_api_url}"
    IDAM_API_HEALTH_URI = "${var.idam_api_url}/health"
  }
}

# region save DB details to Azure Key Vault
module "key-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${local.vaultName}"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.div-emca.resource_group_name}"

  # dcd_cc-dev group object ID
  product_group_object_id = "1c4f0704-a29e-403d-b719-b90c34ef14c9"
}

data "azurerm_key_vault" "div_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "div_doc_s2s_auth_secret" {
  name      = "div-doc-s2s-auth-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}
