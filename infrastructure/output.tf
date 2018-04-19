output "microserviceName" {
  value = "${var.product}"
}

output "vaultName" {
  value = "${module.key_vault.key_vault_name}"
}

output "vaultUri" {
  value = "${module.key_vault.key_vault_uri}"
}

output "test_enviroment" {
  value = "${local.local_env}"
}

output "s2s_url" {
  value = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "dm_store_app_url" {
  value = "http://${var.dm_store_app_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

