locals {
  app_full_name = "${var.product}-${var.component}"
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  dm_store_url = "http://${var.dm_store_app_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
  dm_store_url_alt = "http://${var.dm_store_app_url}-${local.local_env}.service.${local.local_env}.platform.hmcts.net"
}

module "div-em-client-api" {
  source       = "git@github.com:hmcts/moj-module-webapp.git?ref=master"
  product      = "${local.app_full_name}"
  location     = "${var.location}"
  env          = "${var.env}"
  ilbIp        = "${var.ilbIp}"
  subscription = "${var.subscription}"
  capacity     = "${var.capacity}"
  is_frontend  = false
  additional_host_name = "${local.app_full_name}-${var.env}.service.${var.env}.platform.hmcts.net"
  https_only = "false"

  app_settings = {
    REFORM_SERVICE_NAME                                   = "${var.component}"
    REFORM_TEAM                                           = "${var.team_name}"
    REFORM_ENVIRONMENT                                    = "${var.env}"
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                  = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
    AUTH_PROVIDER_SERVICE_CLIENT_MICROSERVICE             = "${var.auth_provider_service_client_microservice}"
    AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.vault_generic_secret.s2s_secret.data["value"]}"
    AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"
    DIVORCE_DOCUMENT_UPLOAD_KEY                           = "${data.vault_generic_secret.s2s_secret.data["value"]}"

    DOCUMENT_MANAGEMENT_STORE_URL       = "${local.dm_store_url}"
    EVIDENCE_MANAGEMENT_UPLOAD_FILE_URL = "${local.dm_store_url}/documents"
    EVIDENCE_MANAGEMENT_HEALTH_URL      = "${local.dm_store_url}/health"
    HTTP_CONNECT_TIMEOUT                = "${var.http_connect_timeout}"
    HTTP_CONNECT_REQUEST_TIMEOUT        = "${var.http_connect_request_timeout}"
    HTTP_CONNECT_SOCKET_TIMEOUT         = "${var.http_connect_socket_timeout}"
  }
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "s2s_secret" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/divorceDocumentGenerator"
}

# region save DB details to Azure Key Vault
module "key_vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.div-em-client-api.resource_group_name}"
  # dcd_cc-dev group object ID
  product_group_object_id = "38f9dea6-e861-4a50-9e73-21e64f563537"
}

resource "azurerm_key_vault_secret" "S2S_TOKEN" {
  name = "s2s-token"
  value = "${data.vault_generic_secret.s2s_secret.data["value"]}"
  vault_uri = "${module.key_vault.key_vault_uri}"
}
