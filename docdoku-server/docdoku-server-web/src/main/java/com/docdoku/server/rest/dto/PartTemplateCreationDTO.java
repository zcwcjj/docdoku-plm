package com.docdoku.server.rest.dto;

import java.util.Set;

public class PartTemplateCreationDTO {

    private String workspaceId;
    private String reference;
    private String partType;
    private boolean idGenerated;
    private String mask;
    private String attachedFiles;
    private Set<InstanceAttributeTemplateDTO> attributeTemplates;
    private boolean attributesLocked;

    private String workflowModelId;
    private boolean workflowLocked;


    public PartTemplateCreationDTO(){
    }

    public PartTemplateCreationDTO(String workspaceId, String partType) {
        this.workspaceId=workspaceId;
        this.partType=partType;
    }


    public String getPartType() {
        return partType;
    }
    public void setPartType(String partType) {
        this.partType = partType;
    }

    public String getMask() {
        return mask;
    }
    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getAttachedFiles() {
        return attachedFiles;
    }
    public void setAttachedFiles(String attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public boolean isIdGenerated() {
        return idGenerated;
    }
    public void setIdGenerated(boolean idGenerated) {
        this.idGenerated = idGenerated;
    }

    public Set<InstanceAttributeTemplateDTO> getAttributeTemplates() {
        return attributeTemplates;
    }
    public void setAttributeTemplates(Set<InstanceAttributeTemplateDTO> attributeTemplates) {
        this.attributeTemplates = attributeTemplates;
    }

    public boolean isAttributesLocked() {
        return attributesLocked;
    }
    public void setAttributesLocked(boolean attributesLocked) {
        this.attributesLocked = attributesLocked;
    }

    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getWorkflowModelId() {
        return workflowModelId;
    }
    public void setWorkflowModelId(String workflowModelId) {
        this.workflowModelId = workflowModelId;
    }

    public boolean isWorkflowLocked() {
        return workflowLocked;
    }
    public void setWorkflowLocked(boolean workflowLocked) {
        this.workflowLocked = workflowLocked;
    }
}
