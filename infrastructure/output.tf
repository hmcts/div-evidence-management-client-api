output "vaultName" {
  value = "${local.vaultName}"
}

output "vaultUri" {
  value = "${local.vaultUri}"
}

output "idam_s2s_url" {
  value = "${local.idam_s2s_url}"
}

output "environment_name" {
  value = "${local.local_env}"
}

output "auth_idam_client_secret" {
    value = "${data.azurerm_key_vault_secret.idam-secret.value}"
}
