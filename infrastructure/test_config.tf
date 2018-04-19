resource "azurerm_key_vault_secret" "idam-auth-secret" {
  name      = "idam-auth-secret"
  value     = "${data.vault_generic_secret.auth_provider_service_client_key.data["value"]}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}