locals {
  aseName        = "core-compute-${var.env}"
  local_env      = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  dm_store_url   = "http://dm-store-${local.local_env}.service.core-compute-${local.local_env}.internal"
  idam_s2s_url   = "http://${var.idam_s2s_url_prefix}-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName = "${var.reform_team}-aat"
  nonPreviewVaultName = "${var.reform_team}-${var.env}"
  vaultName = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"
  vaultUri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
  
  asp_name = "${var.env == "prod" ? "div-emca-prod" : "${var.raw_product}-${var.env}"}"
  asp_rg = "${var.env == "prod" ? "div-emca-prod" : "${var.raw_product}-${var.env}"}"
}

data "azurerm_key_vault" "div_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "div_doc_s2s_auth_secret" {
  name      = "div-doc-s2s-auth-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-secret" {
  name      = "idam-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}
