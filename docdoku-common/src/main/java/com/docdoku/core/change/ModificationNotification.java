/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.docdoku.core.change;

import com.docdoku.core.product.PartIteration;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class which instances are attached to assemblies in order to track
 * modification that happened on sub-component.
 * Its purpose is to notify users that those assemblies may be impacted by these modifications.
 *
 * @author Florent Garin
 * @version 2.0, 12/03/15
 * @since V2.0
 */
@Entity
@NamedQueries ({
        @NamedQuery(name="ModificationNotification.findByImpactedPartIteration", query = "SELECT n FROM ModificationNotification n WHERE n.impactedPart.iteration = :iteration AND n.impactedPart.partRevision.version = :version AND n.impactedPart.partRevision.partMaster.number = :partNumber AND n.impactedPart.partRevision.partMaster.workspace.id = :workspaceId")
})
@Table(name="MODIFICATIONNOTIFICATION")
public class ModificationNotification implements Serializable {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    protected int id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="MODIFIED_ITERATION", referencedColumnName="ITERATION"),
            @JoinColumn(name="MODIFIED_PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
            @JoinColumn(name="MODIFIED_PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
            @JoinColumn(name="MODIFIED_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartIteration modifiedPart;


    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name="IMPACTED_ITERATION", referencedColumnName="ITERATION"),
            @JoinColumn(name="IMPACTED_PARTMASTER_PARTNUMBER", referencedColumnName="PARTMASTER_PARTNUMBER"),
            @JoinColumn(name="IMPACTED_PARTREVISION_VERSION", referencedColumnName="PARTREVISION_VERSION"),
            @JoinColumn(name="IMPACTED_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID")
    })
    private PartIteration impactedPart;

    public ModificationNotification() {
    }

    public int getId() {
        return id;
    }

    public PartIteration getImpactedPart() {
        return impactedPart;
    }

    public void setImpactedPart(PartIteration impactedPart) {
        this.impactedPart = impactedPart;
    }

    public PartIteration getModifiedPart() {
        return modifiedPart;
    }

    public void setModifiedPart(PartIteration modifiedPart) {
        this.modifiedPart = modifiedPart;
    }
}