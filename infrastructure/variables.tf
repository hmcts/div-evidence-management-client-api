variable "reform_service_name" {
  default = "em-client-api"
}

variable "reform_team" {
  default = "div"
}

variable "env" {
  type = "string"
}

variable "evidence_management_client_api_port" {
  default = "4006"
}

variable "auth_provider_service_client_baseurl" {
  type = "string"
}

variable "auth_provider_service_client_microservice" {
  default = "divorce_document_upload"
}

variable "auth_provider_service_client_tokentimetoliveinseconds" {
  default = "900"
}

variable "document_store_url" {
  default = "dm-store-app"
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
