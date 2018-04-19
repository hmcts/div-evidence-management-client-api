output "vaultName" {
   value = "${module.key-vault.key_vault_name}"
}

output "vaultUri" {
  value = "${module.key-vault.key_vault_uri}"
}

output "microserviceName" {
  value = "${var.component}"
}