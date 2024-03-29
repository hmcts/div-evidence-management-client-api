variable "reform_service_name" {
  default = "emca"
}

variable "product" {}

variable "raw_product" {
  default = "div"
}

variable "component" {}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "reform_team" {
  default = "div"
}

variable "capacity" {
  default = "1"
}

variable "instance_size" {
  default = "I2"
}

variable "env" {}

variable "idam_s2s_url_prefix" {
  default = "rpe-service-auth-provider"
}

variable "auth_provider_service_client_microservice" {
  default = "divorce_document_generator"
}

variable "auth_provider_service_client_tokentimetoliveinseconds" {
  default = "900"
}

variable "http_connect_timeout" {
  default = "60000"
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default = ""
}

variable "http_connect_request_timeout" {
  default = "60000"
}

variable "http_connect_socket_timeout" {
  default = "1000"
}

variable "subscription" {}

variable "location" {
  default = "UK South"
}

variable "vault_env" {}

variable "idam_api_url" {}

variable "common_tags" {
  type = map(string)
}
