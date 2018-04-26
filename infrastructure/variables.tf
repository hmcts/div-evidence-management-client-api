variable "reform_service_name" {
  default = "emca"
}

variable "product" {
  type    = "string"
}

variable "component" {
  type = "string"
}

variable "tenant_id" {}

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  type        = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "reform_team" {
  default = "div"
}

variable "env" {
  type = "string"
}

variable "idam_s2s_url_prefix" {
  default = "rpe-service-auth-provider"
}

variable "auth_provider_service_client_microservice" {
  default = "divorce_document_generator"
}

variable "auth_provider_service_client_key" {
  default = "{{ evidence_management_client_api.secret }}"
}

variable "auth_provider_service_client_tokentimetoliveinseconds" {
  default = "900"
}

variable "http_connect_timeout" {
  default = "60000"
}

variable "http_connect_request_timeout" {
  default = "60000"
}

variable "http_connect_socket_timeout" {
  default = "1000"
}

variable "subscription" {}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "ilbIp" {}

variable "vault_env" {}
