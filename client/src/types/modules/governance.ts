export interface DictDiagnosisVO {
  id: number
  code: string
  nameZh: string
  nameEn?: string
}

export interface DictDiagnosisCreateRequest {
  code: string
  nameZh: string
  nameEn?: string
}

export interface CleaningRuleVO {
  id: number
  ruleName: string
  ruleType?: string
  expressionJson?: string
  status?: string
  createdAt?: string
}

export interface CleaningRuleCreateRequest {
  ruleName: string
  ruleType?: string
  expressionJson?: string
}

export interface PublishRuleVO {
  id: number
  ruleName: string
  targetTable?: string
  mappingJson?: string
  status?: string
  createdAt?: string
}

export interface PublishRuleCreateRequest {
  ruleName: string
  targetTable?: string
  mappingJson?: string
}

export interface CrfFormVO {
  id: number
  formCode: string
  formName: string
  specialtyType?: string
  version?: number
  schemaJson?: string
  scoreRulesJson?: string
  status?: string
  publishedAt?: string
  createdAt?: string
}

export interface CrfFormCreateRequest {
  formCode: string
  formName: string
  specialtyType?: string
  schemaJson?: string
  scoreRulesJson?: string
}

export interface CrfResponseVO {
  id: number
  formId: number
  empiId?: number
  responseJson?: string
  status?: string
  submittedAt?: string
}

export interface CrfResponseSubmitRequest {
  formId: number
  empiId?: number
  responseJson: string
}

export interface MetadataCatalogVO {
  id: number
  tableName?: string
  columnName?: string
  dataType?: string
  description?: string
  sourceSystem?: string
}

export interface LineageEdgeVO {
  sourceTable?: string
  targetTable?: string
  transformType?: string
}
